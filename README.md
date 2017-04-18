# FareyPreenFM2
Farey sequence family ratios generator/programmer for the PreenFM2 synthesizer written in Processing
David Burraston
April 2017
http://www.noyzelab.com

** YOU WILL ALSO NEED The MidiBus installed in your Processing library for this program to talk to your PreenFM2 :
 http://www.smallbutdigital.com/projects/themidibus/
Program assumes that your PreenFM2 is directly connected via USB.. !

commands =>
  
 'z': //decrement farey order
 'x': //increment farey order
 
 'c' : //decrement family index
 'v' : //increment family index
 
 'b': // decrement carrier (op1) family member  
 'n': // increment carrier (op1)  family member 
 
 'm': // decrement carrier (op2) family member  
 ',': // increment carrier (op2) family member 

'r': // randomize all farey parameters

's': // send all integer ratios (C) to preenfm op1&3 pair and op2&4 pair
'1' : // send intetger ratios to preenfm  op1&3 pair 
'2' : // send intetger ratios to preenfm  op1&3 pair 

'f': // send as floating point ratios (FLC) to preenfm  op1&3 pair and op2&4 pair modulator normalised to 1.00
'!' : // send as floating point ratios to preenfm  op1&3 pair modulator normalised to 1.00
'@' : // send as floating point ratios to preenfm op2&4 pair modulator normalised to 1.00

'g': // send as floating point ratios to preenfm (FLM) op1&3 pair and op2&4 pair carrier normalised to 1.00
'-' : // send as floating point ratios to preenfm  op1&3 pair carrier normalised to 1.00
'=' : // send as floating point ratios to preenfm  op2&4 pair carrier normalised to 1.00

'k': //  do a minimal voice reconfig for receiving params: sets ALG4, filter off & gain 1.0, IM1&2 and velocity 
          response to 8, mix 1 & 2 to 1.0, IM3&4 off and velocity to 0, op1,2,3,4 to sin with keyboard tracking

'w' : // set waves 1 to 4 to sine & keytrking to kybd

'4' : // set ALG to 4

 <=========
 ** The maximum frequency ratio setting on the PreenFM2 is 16, 
 ** so attempting to send values higher than 16 will be clipped at 16 by PreenFM2 .. ! 
 <=========
 
 NOTES: 
 
 The choice of Carrier:Modulator (C:M) frequency ratio is an interesting area of research, 
 because it is one of the fundamental aspects of FM programming.  Farey Sequences for FM programming were 
 introduced in the classic text by Barry Truax: 
 Organizational Techniques for C:M Ratios in Frequency Modulation. http://www.sfu.ca/~truax/
 
 FareyPreenFM2 is designed to make two carrier/modulator pairs based on the Farey Sequence, and so it uses Algorithm 4 .. 
 so you could also use it with Algorithm 2 for programming Op1/Op2, or Alogrithm 12, or anyhow you like 
 of course.. at the moment its only really designed for generating up to 2 C:M pairs. When/if I get time I'll hope
 to add more commands for mapping with the other FM algorithms and higher order Farey Sequences..
 
 In order to get the best from this program please read up on Farey Sequences for FM programming, 
 I did an article here and there are lots of places to read further :
 http://noyzelab.blogspot.com/2016/04/farey-sequence-tables-for-fm-synthesis.html
 
 I would recommend starting from an init patch or something like that first,
 i.e. turn off all matrix modulation, sin waves for ops etc etc while you are researching Farey Sequence ratios
 and then try loading in existing patches and modifying them, remember to set Algorithm to 4 for the ratios to 
 be set up as C:M pairs. You can use number key '4' to setup ALG4 on PreenFM2, or do a slightly larger reconfig 
 of the voice by pressing 'k' (see key commands above). Press 'r' to randomize your choice of Farey Sequence pair etc.
 AND remember that nothing gets sent to the PreenFM2 unless you send it! e.g. press '1' to send the integer ratio 
 diplayed in the left C:M pair boxes etc.
 
 Family index refers to which C:M pair from a common M value (Family Order). 
 Not all Farey C:M ratios have multiple common M values e.g. Family Order 4 has only one member 1:4, 
 but Family Order 9 has three members : 1:9, 2:9 and 4:9. 
 
 Each Family ratio can then be used to obtain a family of integer ratios sharing the same spectral identity. These are 
 the vertical listings on the screen, C+, C- are the standard integer versions as defined in the research, and FLC and FLM are versions made into 
 floating point values using 2 simple methods I made up so I can;t vouch for their spectral validity.. :]
 
 FLC = C+/M:M/M & C-/M:M/M i.e. divide by modulator, resulting in constant modulator values of 1.0
 MLC = C+/C+:M/C+ & C-/C-:M/C- i.e. divide by carrier,resulting in constant carrier values of 1.0
 
You can also use this program for generating ratios to manually program into other FM synths e.g. TX81Z, DX7 etc. 

BUT: this program was written in Processing : https://processing.org/ and at the moment the sysex in Processing, 
and hence.. .. Java.. .. , is pretty much non-existant for OS X!! I have tried the suggested method from MIDIBus : 

http://www.smallbutdigital.com/projects/themidibus/

but it don;t work on my machine.. so at the moment sysex only works under Windows in Processing for me.. ! 
So I am not including any MIDI sysex until I can get it going on OS X. There also doesn;t seem to be much key repeat action 
on my machine, so this may or may not work for the key commands.. 



