import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import themidibus.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class FareyPreenFM2 extends PApplet {

// Farey Preen V0.2
// April 2017 David Burraston
//
//

 //Import the library
 
MidiBus myBus; // The MidiBus

// Farey sequence NF up to 16 sorted by modulator
// NOTE: have removed all trivial cases where numerator[carrier] is just 1, and hard coded below, 
// this is for cases 1 2 3 4 & 6
int [] farey5mod = {1, 2};
int [] farey7mod  = {1,2,3};  
int [] farey8mod  = {1,3};  
int [] farey9mod  = {1,2,4};  
int [] farey10mod = {1,3};  
int [] farey11mod = {1,2,3,4,5}; 
int [] farey12mod = {1,5};
int [] farey13mod = {1,2,3,4,5,6};
int [] farey14mod = {1,3,5};
int [] farey15mod = {1,2,4,7};
int [] farey16mod = {1,3,5,7};

int [] famratios = new int[100];
float [] flfamratios = new float[100];

int nummembers = 16; // number of family members
int maxnummembers = 20;
int famc, famm; // carrier & modulator values of current family i.e. the farey value

int fareyorder = 2; // farey order to select
int fareyidx = 0; // index into the farey array
int fareymodlength = 1;

int alg = 4;
 
int c1,m3,c2,m4; // variables for alg 4 stack c:m ratios
float c1f,c2f, m3f,m4f; // float versions

// index values for the carriers in the current family ratios array 
int c1idx = 0;
int c2idx = 1;


int oprectsize = 70;
int op13rectleft = 460;

int channel = 0;

int nrpnlsb, nrpnmsb;
  
PFont f;

public void setup(){
  
  f = createFont("Ariel", 16);
  textFont(f);
textAlign(RIGHT);
background(0);
noFill();
    stroke(255);
  MidiBus.list(); // List all available Midi devices on STDOUT. This will show each device's index and name.
  myBus = new MidiBus(this, 0, "PreenFM mk2"); // Create a new MidiBus object
  
  recompute();  
}

public void draw(){
 
}

public void keyPressed() 
{  
switch(key) { 
  case '1' :
   send13pair(c1*100,m3*100);  
  break;
   case '2' :
   send24pair(c2*100,m4*100);  
  break; 
  case '!': // send floats to preenfm   
   // note this is implied as 1 by dividing by family ratio to get the float array
   send13pair((int)(c1f*100),100); // modulator is implied as 1
    break;   
  case '@': // send floats to preenfm   
   // note this is implied as 1 by dividing by family ratio to get the float array
   send24pair((int)(c2f*100),100);  // modulator is implied as 1  
    break;  
  case '-' : // send floats to preenfm   
   send13pair(100,(int)(m3f*100)); // modulator is implied as 1 
  break;
  case '=' : // send floats to preenfm   
   send24pair(100,(int)(m4f*100)); // modulator is implied as 1 
  break;
  case '4' : //send alg 4 cmd to preen
    myBus.sendControllerChange(channel, 16, 3);     
  break;
  case 'z': //decrement farey order
  fareyorder = fareyorder - 1;
  if (fareyorder < 1) {
    fareyorder = 1;
  }  
  fareyidx = 0;
  recompute();
  break;  
  case 'x': //increment farey order
  fareyorder = fareyorder + 1;
  if (fareyorder > 16) {
    fareyorder = 16;
  }  
  fareyidx = 0;
    recompute();
   break;   
  case 'c' : //decrement farey index
  fareyidx = fareyidx - 1;
  if (fareyidx < 0) {
    fareyidx = 0;
  }  
    recompute(); 
  break; 
   case 'v' : //increment farey index
  fareyidx = fareyidx +1;
  if (fareyidx > fareymodlength-1) {
    fareyidx = fareymodlength-1; 
  }  
  recompute();
  break;    
  case 'b': // decrement c1 family member    
  c1idx = c1idx - 1;
  if (c1idx < 0) {
    c1idx = -1;
  }    
  recompute();
  break;   
  case 'n': // increment c1 family member 
  c1idx = c1idx +1;
  if (c1idx > (nummembers*2)-1) {
    c1idx = (nummembers*2)-1; 
  }     
  recompute();
  break;    

  case 'm': // decrement c2 family member    
  c2idx = c2idx - 1;
  if (c2idx < 0) {
    c2idx = -1;
  }    
  recompute();
  break;   
  case ',': // increment c2 family member 
  c2idx = c2idx +1;
  if (c2idx > (nummembers*2)-1) {
    c2idx = (nummembers*2)-1; 
  }     
  recompute();
  break;  
  
 case 'r': // randomize all  
    fareyorder = 1+PApplet.parseInt(random(15));
    fareyidx = PApplet.parseInt(random(5));
    c1idx = PApplet.parseInt(random(15));
    c2idx = PApplet.parseInt(random(15));    
    recompute();
    break;        
  
  case 's': // send to preenfm   
   // note that MIDI data value is 100 * screen value
   send13pair(c1*100,m3*100);
   send24pair(c2*100,m4*100);    
    break;      
  case 'f': // send as carrier floats to preenfm   
   // note this is implied as 1 by dividing by family ratio to get the float array
   send13pair((int)(c1f*100),100); // modulator is implied as 1
   send24pair((int)(c2f*100),100);  // modulator is implied as 1  
    break;        
  case 'g': // send as modulator floats to preenfm   
   // note this is implied as 1 by dividing by family ratio to get the float array
   send13pair(100,(int)(m3f*100)); // carrier is implied as 1 
   send24pair(100,(int)(m4f*100)); // carrier is implied as 1 
    break;    
  case 'k': // full KONFIG
  konfig();
  break;
  case 'w' : // set waves 1 to 4 to sine & keytrking to kybd
      for (int blah = 0x2c; blah < 0x39; blah = blah +4) //SET OP1 to OP4 WAVEFORMS TO SINE
      {
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, blah);     
    myBus.sendControllerChange(channel, 0x06, 0);
    myBus.sendControllerChange(channel, 0x26, 0x0);  
      }  
   for (int blah = 0x2D; blah < 0x40; blah = blah +4) //SET OP1 to OP4 trking to keybd
      {
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, blah);     
    myBus.sendControllerChange(channel, 0x06, 0);
    myBus.sendControllerChange(channel, 0x26, 0x0);  
      }
  break;
  default:                
    break;
  }  
}

public void recompute()
{
    clear();
    computefamcfamr(); 
    computefamratios(nummembers,famc,famm);
    computealg4c1ratio();
    computealg4c2ratio();    
    printfamratios(nummembers,famc,famm);       
}

public void computefamcfamr()
{ 
  switch (fareyorder) {    
    case 1 :
    famc = 1;  
    famm = 1;  
    fareymodlength = 1; 
    break;       
     case 2 :
    famc = 1;  
    famm = 2;  
    fareymodlength = 1;    
    break;    
    case 3 :
    famc = 1;  
    famm = 3;  
    fareymodlength = 1; 
    break;    
    case 4 :
    famc = 1;  
    famm = 4;  
    fareymodlength = 1;  
    break;    
    case 5 :
    famc = farey5mod[(fareyidx%farey5mod.length)];  
    famm = 5;   
    fareymodlength = farey5mod.length;    
    break;    
    case 6 :
    famc = 1;  
    famm = 6;   
    fareymodlength = 1;    
    break;        
    case 7 :
    famc = farey7mod[(fareyidx%farey7mod.length)];  
    famm = 7;   
    fareymodlength = farey7mod.length;    
    break;        
    case 8 :
    famc = farey8mod[(fareyidx%farey8mod.length)];  
    famm = 8;  
    fareymodlength = farey8mod.length;    
    break;        
    case 9 :
    famc = farey9mod[(fareyidx%farey9mod.length)];  
    famm = 9;   
    fareymodlength = farey9mod.length;    
    break;        
    case 10 :
    famc = farey10mod[(fareyidx%farey10mod.length)];  
    famm = 10; 
    fareymodlength = farey10mod.length;    
    break;        
    case 11 :
    famc = farey11mod[(fareyidx%farey11mod.length)];  
    famm = 11;   
    fareymodlength = farey11mod.length;    
    break;        
    case 12 :
    famc = farey12mod[(fareyidx%farey12mod.length)];  
    famm = 12; 
    fareymodlength = farey12mod.length;    
    break;        
    case 13 :
    famc = farey13mod[(fareyidx%farey13mod.length)];  
    famm = 13;   
    fareymodlength = farey13mod.length;    
    break;        
    case 14 :
    famc = farey14mod[(fareyidx%farey14mod.length)];  
    famm = 14;   
    fareymodlength = farey14mod.length;    
    break;                    
    case 15 :
    famc = farey15mod[(fareyidx%farey15mod.length)];  
    famm = 15;   
    fareymodlength = farey15mod.length;
    break;     
    case 16 :
    famc = farey16mod[(fareyidx%farey16mod.length)];  
    famm = 16;   
    fareymodlength = farey16mod.length;    
    break;                     
  default:                
    break;
  }// end switch fareyorder  
}


public void computefamratios(int n, int c, int m)
{
 int cnt = 1; // variable to use for n value in equation
for (int i = 1; i < 2*(n+1); i = i+2) {
  famratios[i-1] = abs(c+cnt*m);  // carrier+ = c+n*m   
  famratios[i] = abs(c-cnt*m);  //carrier- = abs(c+n*m)  
  //floating point normalised versions n:1 i.e. divided by modulator
  flfamratios[i-1] = (float)famratios[i-1]/m;  // carrier+ = c+n*m
  flfamratios[i] = (float)famratios[i]/m;  //carrier- = abs(c+n*m)
  cnt++;
  }  
}

public void computealg4c1ratio()
{
  if (c1idx < 0){ // check if its root family member from the Farey Sequence
  c1 = famc; 
  c1f = (float)famc/famm;
  }
  else // select numerator from the famratios array  
  {
  c1 = famratios[c1idx];     
  c1f = (float)c1/famm;
  } 
  m3 = famm;
  m3f = (float)famm/c1;
}

public void computealg4c2ratio()
{
  if (c2idx < 0){ // check if its root family member from the Farey Sequence
  c2 = famc; 
  c2f = (float)famc/famm;  
  }
  else // select numerator from the famratios array  
  {
  c2 = famratios[c2idx];  
  c2f = (float)c2/famm;  
  } 
  m4 = famm;  
  m4f = (float)famm/c2;
}

public void printfamratios(int n, int c, int m)
{
  clear();
   int cnt2 = 1; // variable to use for graphic offset value 
  text("Family ",80+op13rectleft,280);
  text(c,80+op13rectleft+20,280);
  text(":",80+op13rectleft+25,280); 
  text(m,80+op13rectleft+45,280);  
  text("Farey Order ",120+op13rectleft,300);
  text(fareyorder,120+op13rectleft+20,300);
  text("Families ",30+op13rectleft+60,320);
  text(fareymodlength,30+op13rectleft+80,320);
   text("Family Index ",125+op13rectleft,340);
  text(fareyidx+1,125+op13rectleft+20,340);
  
  text("C+   C-       FLC+     FLC-     FLM+    FLM- ",380,60);
  for (int i = 0; i < (2*n); i = i+2) {
    text(famratios[i],60,80+cnt2);
    text(famratios[i+1],100,80+cnt2);
    text(flfamratios[i],180,80+cnt2);
    text(flfamratios[i+1], 240,80+cnt2);   
    text((float)famm/famratios[i], 320,80+cnt2);   
    text((float)famm/famratios[i+1], 380,80+cnt2);  
   cnt2=cnt2+18;
  }  
  
  // DRAW ALG 4 boxes and values
  strokeWeight(4);
    fill(255); // colour to white
   rect(op13rectleft, 50, oprectsize, oprectsize); //m3 box 
   line(op13rectleft+oprectsize/2, 50+oprectsize, op13rectleft+oprectsize/2, 77+oprectsize); 
    rect(op13rectleft, 150, oprectsize, oprectsize); //c1 box
   rect(op13rectleft+100, 50, oprectsize, oprectsize); //m4 box 
   line(op13rectleft+100+oprectsize/2, 50+oprectsize, op13rectleft+100+oprectsize/2, 77+oprectsize); 
    rect(op13rectleft+100, 150, oprectsize, oprectsize); //c2 box    
    fill(0); // set text colour to black
   text(m3,op13rectleft+60,80);
   text("1.00",op13rectleft+60,100);
   text(m3f,op13rectleft+60,120);
   
   text(c1,op13rectleft+60,180);
   text(c1f,op13rectleft+60,200);
   text("1.00",op13rectleft+60,220);
   
   text(m4,op13rectleft+160,80);
   text("1.00",op13rectleft+160,100); 
   text(m4f,op13rectleft+160,120);
   
   text(c2,op13rectleft+160,180);
   text(c2f,op13rectleft+160,200);
    text("1.00",op13rectleft+160,220);  
   fill(255); //reset text colour to white   
   
   //println("Carrier1 = ",c1," Carrier1 FL = ",c1f, "Modulator3 = ",m3, "Modulator3 FL = ",m3f, "Modulator4 FL = ",m4f);  

}

public void    send13pair(int car, int mod)
{  
    int carlsb,carmsb,modlsb,modmsb; 
    
    carmsb = car >> 7 & 0x7F;
    carlsb = car & 0x7F;
    modmsb = mod >> 7 & 0x7F;
    modlsb = mod & 0x7F; 
    // ALG 4
    // left carrier OPERATOR 1
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, 0x2E);     
    myBus.sendControllerChange(channel, 0x06, carmsb);
    myBus.sendControllerChange(channel, 0x26, carlsb);         
    // left modulator OPERATOR 3
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, 0x36);     
    myBus.sendControllerChange(channel, 0x06, modmsb);
    myBus.sendControllerChange(channel, 0x26, modlsb);       
}

public void    send24pair(int car, int mod)
{  
    int carlsb,carmsb,modlsb,modmsb;
  
    carmsb = car >> 7 & 0x7F;
    carlsb = car & 0x7F;
    modmsb = mod >> 7 & 0x7F;
    modlsb = mod & 0x7F; 
    // ALG 4
    // right carrier OPERATOR 2
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, 0x32);     
    myBus.sendControllerChange(channel, 0x06, carmsb);
    myBus.sendControllerChange(channel, 0x26, carlsb);        
    // right modulator OPERATOR 4
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, 0x3A);     
    myBus.sendControllerChange(channel, 0x06, modmsb);
    myBus.sendControllerChange(channel, 0x26, modlsb);       
}

public void konfig() // full configuration change to set up preen to have alg 4, sin waves, mix,etc
{  
    myBus.sendControllerChange(channel, 16, 3);          //send alg 4 
     myBus.sendControllerChange(channel, 70, 0);          //filter off
         
    nrpnmsb = 100 >> 7 & 0x7F; 
    nrpnlsb = 100 & 0x7F;
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, 0x2B);     // filter gain = 1
    myBus.sendControllerChange(channel, 0x06, nrpnmsb);
    myBus.sendControllerChange(channel, 0x26, nrpnlsb); 
    
    myBus.sendControllerChange(channel, 22, 127);          //set IM1 mix = 1            
    myBus.sendControllerChange(channel, 24, 127);          //set IM2 mix = 1   
    
    myBus.sendControllerChange(channel, 17, 80);          //set IM1 = 8
    myBus.sendControllerChange(channel, 18, 80);          //set IM2 = 8    
    myBus.sendControllerChange(channel, 19, 0);          //set IM3 = 0
    myBus.sendControllerChange(channel, 20, 0);          //set IM4 = 0     

    // set velocity value to 8 so send 8*100 as lsb / msb
    nrpnmsb = 800 >> 7 & 0x7F; 
    nrpnlsb = 800 & 0x7F; 
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, 0x5);     // IM1
    myBus.sendControllerChange(channel, 0x06, nrpnmsb);
    myBus.sendControllerChange(channel, 0x26, nrpnlsb); 
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, 0x7);     // IM2
    myBus.sendControllerChange(channel, 0x06, nrpnmsb);
    myBus.sendControllerChange(channel, 0x26, nrpnlsb);     
    
    // set IM3&4 velocity value to 0  
    nrpnmsb = 800 >> 7 & 0x7F; 
    nrpnlsb = 800 & 0x7F; 
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, 0x9);     // IM3
    myBus.sendControllerChange(channel, 0x06, 0);
    myBus.sendControllerChange(channel, 0x26, 0); 
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, 0xB);     // IM4
    myBus.sendControllerChange(channel, 0x06, 0);
    myBus.sendControllerChange(channel, 0x26, 0);         
        
       
      for (int blah = 0x2c; blah < 0x39; blah = blah +4) //SET OP1 to OP4 WAVEFORMS TO SINE
      {
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, blah);     
    myBus.sendControllerChange(channel, 0x06, 0);
    myBus.sendControllerChange(channel, 0x26, 0x0);  
      }
      
          for (int blah = 0x2D; blah < 0x40; blah = blah +4) //SET OP1 to OP4 trking to keybd
      {
    myBus.sendControllerChange(channel, 0x63, 0x0);
    myBus.sendControllerChange(channel, 0x62, blah);     
    myBus.sendControllerChange(channel, 0x06, 0);
    myBus.sendControllerChange(channel, 0x26, 0x0);  
      }
    
}
  public void settings() {  size(650, 380); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "FareyPreenFM2" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
