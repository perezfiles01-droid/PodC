import json,numpy as np,soundfile as sf
D=json.load(open('durs.json'));SR=24000;tl=[];t=4.6;prev=-1
for si,li,d in D:
  if si!=prev and prev!=-1: t+=0.9
  tl.append([si,li,t,t+d]);t+=d+0.7;prev=si
T=t+2.5;json.dump({'tl':tl,'T':T},open('timeline.json','w'))
n=np.zeros(int(T*SR))
for si,li,a,b in tl:
  x,_=sf.read(f'n_{si}_{li}.wav');i=int(a*SR);n[i:i+len(x)]+=x
m=np.zeros_like(n);rng=np.random.default_rng(1)
def pl(f,t0,v,dur):
  i=int(t0*SR);k=np.arange(int(dur*SR));
  if i>=len(m):return
  env=np.exp(-k/SR*3.2/dur*1.5)*np.minimum(1,k/(0.01*SR));w=(2/np.pi)*np.arcsin(np.sin(2*np.pi*f*k/SR))
  e=min(len(m),i+len(k));m[i:e]+=(v*env*w)[:e-i]
notes=[0,2,4,5,7,9,11,12];chords=[[0,4,7],[5,9,12],[7,11,14],[0,4,7]];b=262;step=0;t0=0
while t0<T:
  for i in range(8):
    w=t0+i*.25;nn=notes[(step*3+i*5+i%3)%8]
    if i%2==0 or rng.random()<.6: pl(b*2*2**(nn/12),w,.3,1.6)
    if i==0:
      for c in chords[step%4]: pl(b/2*2**(c/12),w,.16,2.6)
  for i in range(8):
    pl(b/4*2**(chords[step%4][0]/12),t0+i*.25,.22 if i%2==0 else 0,.2)
    if i%2: [pl(b/2*2**(c/12),t0+i*.25,.07,.18) for c in chords[step%4][1:]]
  step+=1;t0+=2.0
cr=rng.standard_normal(len(m))*0.004;pops=rng.random(len(m))<0.0006;cr[pops]+=rng.standard_normal(pops.sum())*0.25
m=m*0.12+cr*0.15
fade=np.minimum(1,np.minimum(np.arange(len(m))/SR/2,(len(m)-np.arange(len(m)))/SR/2.5));m*=fade
out=n*0.95+m;out/=max(1,np.abs(out).max()/0.95)
sf.write('mix.wav',out,SR);print(T)
