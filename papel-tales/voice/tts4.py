import json,re,sys,os,soundfile as sf
sys.path.insert(0,os.path.dirname(os.path.abspath(__file__)));import heteronyms
from kokoro_onnx import Kokoro
K=Kokoro('../voice/kokoro-v1.0.onnx','../voice/voices-v1.0.bin')
F=json.load(open('fil.json'));FL={k.lower():v for k,v in F.items()}
rx=re.compile(r'\b('+'|'.join(map(re.escape,sorted(F,key=len,reverse=True)))+r')\b(’s)?',re.I)
YEARS={}
PH=['apple','zebra','violin','marble','tulip','walrus','pepper','candle','rocket','button','pillow','ladder','basket','helmet','muffin','window']
AUDIT=[];WORDS={}
def spoken(t):
  t=t.replace('"','').replace('“','').replace('”','')
  for y,w in YEARS.items(): t=re.sub(r'\b'+y+r's?\b',w,t)
  t=t.replace('ABS-CBN','A B S C B N').replace('RTL 4','R T L four').replace('ITV','I T V').replace('NBC','N B C').replace('X Factor','Ex Factor').replace('TV ','T V ').replace('...',',')
  return t
def ph(t):
  t=spoken(t);subs=[]
  t,forced=heteronyms.apply(t)
  def rep(m):
    i=len(subs);subs.append(FL[m.group(1).lower()]+('z' if m.group(2) else ''));return PH[i]
  t2=rx.sub(rep,t)
  def frep(m):
    i=len(subs);subs.append(forced[int(m.group(1))]);return PH[i]
  t2=re.sub('\x00(\\d+)\x00',frep,t2);whole=K.tokenizer.phonemize(t2,'en-us')
  for i,ipa in enumerate(subs):
    w=K.tokenizer.phonemize(PH[i],'en-us').strip(' .')
    if w not in whole: raise SystemExit(f'placeholder {PH[i]} ({w}) not found in: {whole}')
    whole=whole.replace(w,ipa,1)
    if not ipa.endswith(('ɹ','ɚ','ɾ')): whole=whole.replace(ipa+'ɹ',ipa,1)
  return whole
L=json.load(open('lines.json'));res=[];dump=[]
# strict pre-check: every word must have one known pronunciation before any audio is made
FW=set(FL)|{x for k in FL for x in k.split()}
issues=[]
for si,s in enumerate(L):
  for li,t in enumerate(s['lines']):
    t2,_=heteronyms.apply(spoken(t));t2=rx.sub(' ',t2)
    for p in heteronyms.strict_check(re.sub('\x00\\d+\x00',' ',t2),FW): issues.append(f'{si}.{li}  {p}')
if issues:
  open('pronunciation_issues.txt','w').write('\n'.join(issues));print('\n'.join(issues));raise SystemExit('STOP: resolve every word above (fil.json, RULES or SAFE), then re-run.')
for si,s in enumerate(L):
  for li,t in enumerate(s['lines']):
    p=ph(t);dump.append(f'{si}.{li}: {p}')
    for w in heteronyms.audit(t): AUDIT.append(f'{si}.{li}  {w:10s} {t}')
    a,sr=K.create(p,voice='af_heart',speed=0.92,is_phonemes=True)
    sf.write(f'n_{si}_{li}.wav',a,sr);res.append([si,li,len(a)/sr])
    # estimated start time of every word (share of phonemes, pauses weighted at punctuation)
    toks=t.split();wts=[max(1,len(re.sub(r'[^\w]','',ph(w))))+(4 if re.search(r'[,.;:?!]$',w) else 0) for w in toks];tot=sum(wts);acc=0;wt=[]
    for w,k in zip(toks,wts): wt.append([re.sub(r'[^\w’\'-]','',w).lower(),round(acc/tot*len(a)/sr,2)]);acc+=k
    WORDS.setdefault(si,{})[li]=wt
sheet={}
for si,s in enumerate(L):
  for t in s['lines']:
    for w in re.findall(r"[A-Za-zÀ-ÿ][A-Za-zÀ-ÿ'’-]*",spoken(t)): sheet.setdefault(w.lower(),ph(w).strip(' .'))
open('pronunciation_sheet.txt','w').write('Every word and the exact sound used\n'+'\n'.join(f'{w:22s} {p}' for w,p in sorted(sheet.items())))
json.dump(res,open('durs.json','w'));json.dump(WORDS,open('words.json','w'));open('phonemes.txt','w').write('\n'.join(dump))
open('pronunciation_audit.txt','w').write('Check each word below sounds right in phonemes.txt before rendering.\n'+'\n'.join(AUDIT));print('\n'.join(AUDIT))
