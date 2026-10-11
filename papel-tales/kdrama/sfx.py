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
EV=[(1,3,'aja','boing'),(4,2,'again','pop'),(9,1,'family','boing')]
for s,l,w,k in EV:
  x=FX[k]()*0.55;i=int(wt(s,l,w)*SR);e=min(len(m),i+len(x));m[i:e]+=x[:e-i]
m/=max(1,np.abs(m).max()/0.97);sf.write('mix.wav',m,SR);print(len(EV),'sfx')
