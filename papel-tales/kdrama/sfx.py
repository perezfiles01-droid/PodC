import json,numpy as np,soundfile as sf
m,SR=sf.read('mix.wav');tl=json.load(open('timeline.json'))['tl'];W=json.load(open('words.json'))
start={(s,l):a for s,l,a,b in tl}
def wt(s,l,w):
  for x,t in W[str(s)][str(l)]:
    if x.startswith(w):return start[(s,l)]+t
  raise SystemExit(f'missing {s}.{l} {w}')
def tone(f0,f1,d,v,kind='sin'):
  k=np.arange(int(d*SR))/SR;f=np.linspace(f0,f1,len(k));ph=2*np.pi*np.cumsum(f)/SR
  w=np.sin(ph) if kind=='sin' else np.sign(np.sin(ph))*.5
  return v*w*np.exp(-k*4/d)*np.minimum(1,k/.005)
FX={'pop':lambda:tone(900,300,.12,.35),'boing':lambda:tone(180,520,.45,.3)*(1+.5*np.sin(np.arange(int(.45*SR))/SR*2*np.pi*14)),
 'slide':lambda:tone(400,1400,.5,.22),'ding':lambda:tone(1320,1320,.6,.25)+tone(1980,1980,.6,.1),'scratch':lambda:tone(1200,200,.25,.25,'sq'),'down':lambda:tone(900,200,.55,.22)}
EV=[(0,1,'episode','ding'),(0,2,'six','pop'),(0,3,'korean','pop'),(1,1,'follow','boing'),(1,2,'cried','down'),(1,3,'aja','boing'),
(2,0,'hard','slide'),(2,2,'breaks','scratch'),(2,2,'goodbye','pop'),(3,1,'have','ding'),(4,1,'slow','slide'),(4,2,'again','pop'),
(5,0,'crazy','pop'),(6,2,'hundred','slide'),(6,2,'year','boing'),(6,3,'searched','ding'),(7,1,'fast','slide'),(7,2,'eighty','ding'),
(8,2,"don't",'scratch'),(8,3,'pretends','boing'),(9,0,'coming','ding'),(9,1,'family','boing')]
for s,l,w,k in EV:
  x=FX[k]();i=int(wt(s,l,w)*SR);e=min(len(m),i+len(x));m[i:e]+=x[:e-i]
m/=max(1,np.abs(m).max()/0.97);sf.write('mix.wav',m,SR);print(len(EV),'sfx')
