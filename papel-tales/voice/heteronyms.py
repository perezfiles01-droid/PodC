"""Pronunciation guard for Kokoro narration (Papel Tales).
Kokoro's phonemizer guesses words that are spelled the same but said differently
(live/lives, read, lead, wind, tear, close...). It got "an aswang lives in the next
village" wrong ("layvz"). Two layers fix this:
 1. RULES: context patterns that force the right sound before phonemizing.
 2. AUDIT: after phonemizing, every heteronym in the line is listed in
    pronunciation_audit.txt with the sound chosen, so it is checked by eye, not by luck.
Add a rule whenever a word is misread; the user's respelling goes in the comment."""
import re
LIV='lˈɪv'; LAIV='lˈaɪv'
# (regex on the spoken text, IPA for group 1). Checked in order; first match wins per word.
RULES=[
 (r'\b(live)\b(?=\s+(shows?|music|audience|tv|broadcast)\b)', LAIV),
 # verb "live/lives/lived/living" (rhymes with "give"): after a subject, or before in/on/with/at/near/here/there/alone/together/long/happily/forever
 (r'\b(lives)\b(?=\s+(in|on|with|at|near|here|there|alone|together|forever|happily|deep|among|by|under|inside|beyond)\b)', LIV+'z'),
 (r'\b(?:who|that|which|it|he|she|one|still|also|now|only|really|and|story|legend|spirit|aswang|creature)\s+(lives)\b', LIV+'z'),
 (r'\b(live)\b(?=\s+(in|on|with|at|near|here|there|alone|together|forever|happily|long|among|by|under|inside|again|like)\b)', LIV),
 (r'\b(?:to|they|we|you|i|people|families|spirits|creatures|still|can|could|would|will|may|might|must|should|did|do|does)\s+(live)\b', LIV),
 (r'\blong\s+(live)\b', LIV),
 (r'\b(lived)\b', LIV+'d'),
 (r'\b(living)\b', 'lˈɪvɪŋ'),
 # noun "lives" (plural of life) -> layvz: "their lives", "many lives", "save lives"
 (r'\b(?:their|our|your|his|her|my|many|nine|two|three|past|whole|saved?|risk|risked|ordinary|daily)\s+(lives)\b', LAIV+'z'),
 # wind: noun (the breeze) = wɪnd; verb (to turn) = waɪnd
 (r'\b(?:the|a|cold|strong|north|south|east|west|night|gentle|warm|monsoon|sea|in)\s+(wind)\b', 'wˈɪnd'),
 (r'\b(?:the|cold|strong|north|south|east|west|night|gentle|warm|monsoon|sea|trade)\s+(winds)\b', 'wˈɪndz'),
 (r'\b(winds?|wound)\b(?=\s+(up|down|around|round|through|along|its|their|the\s+clock)\b)', 'wˈaɪnd'),
 # record: noun REK-ord, verb ri-KORD
 (r'\b(?:written|the|old|oldest|earliest|early|historical|church|colonial|police|world|these|those|their|its|official|public|birth|court|baptismal|a|no)\s+(records)\b', 'ɹˈɛkɚdz'),
 (r'\b(?:written|the|old|oldest|earliest|historical|church|colonial|police|world|a|no|official|public)\s+(record)\b', 'ɹˈɛkɚd'),
 (r'\b(?:to|will|would|could|did|they|we|helped|began\sto)\s+(record)\b', 'ɹᵻkˈɔːɹd'),
 (r'\b(recorded)\b', 'ɹᵻkˈɔːɹdᵻd'),
 # read: present REED, past RED
 (r'\b(?:to|will|would|could|can|should|must|may|might|please|let\'s|i|we|you|they|children|people)\s+(read)\b(?!\s+(?:it\s+)?(?:yesterday|long\sago|once|before|years\sago))', 'ɹˈiːd'),
 (r'\b(?:had|has|have|was|were|been|already|once|first|being)\s+(read)\b', 'ɹˈɛd'),
 (r'\b(read)\b(?=\s+(?:it\s+)?(?:yesterday|long\sago|once|before|years\sago))', 'ɹˈɛd'),
 # adjective "live" (LAIV): live music, live show, live broadcast, live wire
 (r'\b(live)\b(?=\s+(music|show|broadcast|wire|animals?|bait|coals?|audience|performance)\b)', LAIV),
]
WATCH=['live','lives','lived','living','read','lead','leads','wind','winds','tear','tears','close','closes','wound','bow','bows','row','rows',
 'minute','object','present','record','content','desert','dove','does','use','used','close','house','moped','bass','sow','refuse','project','permit','produce','invalid','resume','polish','wicked','learned','aged','blessed']
def apply(text):
    """Return text with forced words replaced by placeholders, plus the IPA list (order kept)."""
    forced=[]
    for pat,ipa in RULES:
        def rep(m,ipa=ipa):
            forced.append(ipa);g=m.group(0);a,b=m.start(1)-m.start(0),m.end(1)-m.start(0)
            return g[:a]+'\x00%d\x00'%(len(forced)-1)+g[b:]
        text=re.sub(pat,rep,text,flags=re.I)
    return text,forced
def audit(text):
    return [w for w in re.findall(r"[A-Za-z']+",text) if w.lower() in WATCH]

# ---------- STRICT MODE: every word must have a known, single pronunciation ----------
# A word passes only if it is (a) in fil.json, (b) forced by RULES, (c) in SAFE, or
# (d) in the CMU dictionary with one reading (accent variants and weak forms like
# "thuh/thee" are ignored). Anything else stops the build until it is given a sound.
import cmudict
_D=cmudict.dict()
_CLASS={'IH':'e','IY':'i','EH':'e','AE':'e','EY':'ey','AA':'o','AO':'o','AH':'o','UH':'o','OW':'ow','UW':'u','AY':'ay','AW':'aw','OY':'oy','ER':'er'}
SAFE={'our','during','rather','tv','inside','sometimes','unfair','because','into','really','caused','us','when','says','again','either','neither','route','data','often','been','were','your','aunt','tomato','envelope','vase','garage','schedule','thirteen','fourteen','fifteen','sixteen','seventeen','eighteen','nineteen','eleven','duo'}  # checked by ear; extend with care
def _stressed(p):
    return tuple(sorted({_CLASS.get(x[:-1],x[:-1]) for x in p if x[-1]=='1'}))
def _known(w):
    if w in _D: return True
    for i in range(3,len(w)-2):  # compounds: shapeshifter = shape + shifter
        if w[:i] in _D and w[i:] in _D: return True
    return all(p in _D for p in w.split('-')) if '-' in w else False
def strict_check(text,fil_words):
    """Return problems in one line after RULES/fil.json words are already replaced by \\x00n\\x00 / placeholders."""
    probs=[]
    for w in re.findall(r"[A-Za-zÀ-ÿ][A-Za-zÀ-ÿ'’-]*",text):
        lw=w.lower().replace('’',"'")
        if lw in fil_words or lw in SAFE: continue
        base=lw[:-2] if lw.endswith("'s") else lw
        if not _known(base):
            probs.append(f'UNKNOWN   {w}: add it to fil.json with IPA'); continue
        if base in _D:
            v={_stressed(p) for p in _D[base]}; v={x for x in v if x}
            if len(v)>1: probs.append(f'AMBIGUOUS {w}: dictionary has {sorted(v)}; add a RULE in heteronyms.py or add to SAFE after listening')
    return probs
