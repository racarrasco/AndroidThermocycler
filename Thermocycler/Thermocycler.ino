/*************************************************** 
  This is an example for the Adafruit Thermocouple Sensor w/MAX31855K

  Designed specifically to work with the Adafruit Thermocouple Sensor
  ----> https://www.adafruit.com/products/269

  These displays use SPI to communicate, 3 pins are required to  
  interface
  Adafruit invests time and resources providing this open source code, 
  please support Adafruit and open-source hardware by purchasing 
  products from Adafruit!

  Written by Limor Fried/Ladyada for Adafruit Industries.  
  BSD license, all text above must be included in any redistribution
 ****************************************************/

#include <SPI.h>
#include "Adafruit_MAX31855.h"

// Default connection is using software SPI, but comment and uncomment one of
// the two examples below to switch between software SPI and hardware SPI:

//// Example creating a thermocouple instance with software SPI on any three
//// digital IO pins.
//#define MAXDO   3
//#define MAXCS   4
//#define MAXCLK  5

//// initialize the Thermocouple
//Adafruit_MAX31855 thermocouple(MAXCLK, MAXCS, MAXDO);

//// Example creating a thermocouple instance with hardware SPI (Uno/Mega only)
//// on a given CS pin.
#define MAXCS   10
//I guess MOSI(Not needed) is 11, MISO is 12, SCK is 13
Adafruit_MAX31855 thermocouple(MAXCS);

#if defined(ARDUINO_ARCH_SAMD)
// for Zero, output on USB Serial console, remove line below if using programming port to program the Zero!
   #define Serial SerialUSB
#endif


// LED controller is plugged into port 9 on the arduino
#define LED_PIN 6
// Fan
#define FAN_PIN 7
#define CMD_LEN 40
#define PROG_LEN 60   // program buffer length
#define DATAQ_LEN 100  // data queue length
char flag_debug;           // debug mode on
int ibuf[CMD_LEN];      // for incoming serial data
int ledval;             // led control value
char cmdarr[PROG_LEN];   // command
int param1arr[PROG_LEN];   // param1
int param2arr[PROG_LEN];   // param2
int param3arr[PROG_LEN];   // param3
int param4arr[PROG_LEN];   // param4
int param5arr[PROG_LEN];   // param5
int timearr[DATAQ_LEN];
int timemilarr[DATAQ_LEN];
int temparr100x[DATAQ_LEN];
int timearr0;   // reference time
float curtemp;
int curtime;
int curtimemil;

char type;         // type ie. heating or cooling
int target;        // target temp
int curcycle;         // current cycle
int cmdcnt;           // command count
int cmdpointer;       // command pointer
int cmdtrig;          // command trig

char pidtrig=0;       // pid trigger
int pidt0;  // pid start time
char precooltrig=0; //precool trigger
int precoolt0; //precool start time
char event_flag=0;    // event logged?
float event_temp;      // saved temperature

double pid_temp=0;
double pid_scale=1; //pid param scale
double pid_in=0;
double pid_pin=0;
double pid_acc=100;
double pid_kp=0;
double pid_ki=0;
double pid_kd=0;
double pid_val=0;

int maxtemp=100;
char ovptrig=0; //over voltage protection
char ovpofftemp=60; //over voltage trigger off temp

// aux functions
int asc2num(int asc) { // Convert ASCII character to number
  return asc - 48;
}
int dig2num(char v2, char v1, char v0) { // Convert 3 digit numbers to integer
  return asc2num((int)v2)*100+asc2num((int)v1)*10+asc2num((int)v0);
}

// controller functions
void turnonFan() { //turn on FAN
  digitalWrite(FAN_PIN, HIGH);
}  
void turnoffFan() { //turn off FAN
  digitalWrite(FAN_PIN, LOW);
}  
void turnonLED(int v) {
  //v=-1*v+255;
  ledval=v;
  analogWrite(LED_PIN, ledval);
}
void turnoffLED() { //turn off LED
  //ledval=255;
  ledval=0;
  analogWrite(LED_PIN, ledval);
}
double getTemp() { //get Temperature
  //for 31855
  return 0.25*(thermocouple.readCelsius()+thermocouple.readCelsius()+thermocouple.readCelsius()+thermocouple.readCelsius()); //averaging
  //return thermocouple.readCelsius();
}

// main functions
void setup() {
  ovptrig=0;
  cmdcnt=0;
  flag_debug=0;
  // initialize queue
  for (int i=0; i<DATAQ_LEN; i++) {
    timearr[i]=0;
    timemilarr[i]=0;
    temparr100x[i]=0;
  }
  timearr0=millis()/1000;
  
  pinMode(LED_PIN, OUTPUT); pinMode(FAN_PIN, OUTPUT);
  ledval=0; analogWrite(LED_PIN, ledval);  
  digitalWrite(FAN_PIN, LOW);
  
  // flush command buffer
  for (int i=0; i<CMD_LEN; i++) {
    ibuf[i]=0;
  }
  
  #ifndef ESP8266
    while (!Serial);     // will pause Zero, Leonardo, etc until serial console opens
  #endif
  //Serial.begin(9600);
  //Serial.begin(14400);
 Serial.begin(19200);
  //Serial.begin(57600);
  //Serial.begin(115200);
  //Serial.begin(250000);
  // wait for MAX chip to stabilize
  delay(500);

}

void loop() {
  curtemp=getTemp(); //get current temp
  //Command run
  if (cmdtrig==1) {
    if (cmdpointer==cmdcnt) {
      turnoffLED();
      turnoffFan();
      cmdpointer=0;
      cmdtrig=0;
    } else {
      curtime=millis()/1000-timearr0; //get current time
      curtimemil=millis()%1000;
      for (int i=1; i<DATAQ_LEN; i++) {
        timearr[i-1]=timearr[i];
        timemilarr[i-1]=timemilarr[i];
        temparr100x[i-1]=temparr100x[i];
      }
      timearr[DATAQ_LEN-1]=curtime; //log time & temp
      timemilarr[DATAQ_LEN-1]=curtimemil;
      temparr100x[DATAQ_LEN-1]=curtemp*100;
      switch (cmdarr[cmdpointer]) {
        case 'R': //reset
          turnoffLED();
          turnoffFan();
          cmdpointer++;
          for (int i=0; i<DATAQ_LEN; i++) { //reset queue and time info
            timearr[i]=0;
            timemilarr[i]=0;
            temparr100x[i]=0;
          }
          timearr0=millis()/1000;
        break;
        case 'L': //loop back
          if (param2arr[cmdpointer]>param3arr[cmdpointer]) {
            param3arr[cmdpointer]++;
            curcycle=param3arr[cmdpointer];
            cmdpointer-=param1arr[cmdpointer];
          } else {
            cmdpointer++;
          }
        break;
        case 'H': //heat
          turnoffFan();
          turnonLED(255);
          type = 'H'; 
          target = param1arr[cmdpointer];
          if (flag_debug) Serial.println(curtemp); 
          if (param1arr[cmdpointer] < curtemp) {
            event_flag=1;
            event_temp=getTemp(); 
            cmdpointer++;
          }
        break;
        case 'C': //cool
          turnoffLED();
          turnonFan();
          type ='C';
          target = param1arr[cmdpointer];
          if (flag_debug) Serial.println(curtemp); 
          if (param1arr[cmdpointer] > curtemp) {
            //Serial.println(Cooling);
            event_flag=1;
            event_temp=getTemp(); 
            cmdpointer++;
          }
        break;
        case 'K': //precool
        turnoffLED();
        turnonFan();
        type = 'K';
        target = 21;
        if(precooltrig==0) { //start
          precoolt0= millis()/100;
          precooltrig=1;
        } else{ 
          if(millis()/100-precoolt0>param1arr[cmdpointer]*10){ //cool for specified time
            precooltrig=0;
            cmdpointer++; 
            } 
          }
        break;
        case 'O': //control
          //param1=Temp 2=Time 3=P 4=I 5=D
          type = 'O';
          target = param1arr[cmdpointer]; 
          if (pidtrig==0) { //start
            pidt0=millis()/100;
            pidtrig=1;
          } else {
            if (millis()/100-pidt0>param2arr[cmdpointer]*10) {
              pidtrig=0;
              cmdpointer++;
            } else {
              //control
              //pid control
              pid_kp=param3arr[cmdpointer]*pid_scale;
              pid_ki=param4arr[cmdpointer]*pid_scale;
              pid_kd=param5arr[cmdpointer]*pid_scale;
              pid_temp=getTemp();
              pid_pin=pid_in;
              pid_in=param1arr[cmdpointer]-pid_temp;
              pid_acc=pid_acc+pid_in;
              pid_val=pid_kp*pid_in+pid_ki*pid_acc;//+pid_kd*(pid_pin-pid_in);
              //pid_val=pid_kp*pid_in+pid_ki*pid_acc+pid_kd*(pid_pin-pid_in);
              if (pid_val>255) {pid_val=255;}
              if (pid_val<0) {pid_val=0;}
              turnonLED(int(pid_val));
              //on/off control
              //if (getTemp()>param1arr[cmdpointer]) {
              //  turnoffLED();
              //} else {
              //  turnonLED(255);
              //}
            }
          }  
        break;
        case 'E': //end
          turnoffLED();
          turnoffFan();
          cmdpointer=0;
          curcycle=1;
          cmdtrig=0;
        break;
      } 
      
    }
  }
  //Over temperature protection
  if (getTemp()>maxtemp) {
    turnoffLED();
    ovptrig=1;
  }
  if (ovptrig==1) {
    turnoffLED();
    if (getTemp()<ovpofftemp)
    ovptrig=0;
  }
  delay(20);
}

void serialEvent() {
  // send data only when you receive data: 
  if (Serial.available() > 0) {
    // read the incoming data:
    for (int i = CMD_LEN-1; i > 0; i--) {
      ibuf[i] = ibuf[i - 1];  //shift
    }
    ibuf[0] = Serial.read();
    char char_term;
    if (flag_debug) char_term='E';
    else char_term=10; //\n
    if (ibuf[0] == char_term) {
      char cmd = (char)ibuf[1];
      if (cmd == '1') { //turn on LED
        int v=dig2num((char)ibuf[4], (char)ibuf[3], (char)ibuf[2]);
        turnonLED(v);
      }
      if (cmd == '0') { //turn off LED
        turnoffLED();
      }
      if (cmd == 'F') { //turn on FAN
        turnonFan(); 
      }
      if (cmd == 'H') { //turn off FAN
        turnoffFan();
      }      
      if (cmd == 'T') { //temperature
        if (event_flag==1) {
          event_flag=0;
          Serial.println(event_temp); 
        } else {
          Serial.println(curtemp); 
        }
      }
      if (cmd == 'P') {// command stop
        cmdpointer = 0;
        cmdtrig = 0;
        curcycle = 1;
        turnoffFan();
        turnoffLED();        
      }
      if (cmd == 'R') { //command reset
        cmdcnt=0;
      }
      if (cmd == 'C') { //command 
        cmdarr[cmdcnt]=(char)ibuf[2];   // command
        param1arr[cmdcnt]=dig2num((char)ibuf[5], (char)ibuf[4], (char)ibuf[3]);
        param2arr[cmdcnt]=dig2num((char)ibuf[8], (char)ibuf[7], (char)ibuf[6]);
        param3arr[cmdcnt]=dig2num((char)ibuf[11], (char)ibuf[10], (char)ibuf[9]);
        param4arr[cmdcnt]=dig2num((char)ibuf[14], (char)ibuf[13], (char)ibuf[12]);
        param5arr[cmdcnt]=dig2num((char)ibuf[17], (char)ibuf[16], (char)ibuf[15]);                
        cmdcnt++;
      }
      if (cmd == 'S') { //status display
        Serial.print(cmdpointer);
        Serial.print(' ');
        Serial.print(curcycle);
        Serial.print(' ');
        Serial.print(type);
        Serial.print(' ');
        Serial.print(target);
        Serial.println();
      }
      if (cmd == 'L') { //log
        for (int i=DATAQ_LEN-50; i<DATAQ_LEN; i++) {
          Serial.print(timearr[i]);
          Serial.print(' ');
          Serial.print(timemilarr[i]);
          Serial.print(' ');
          Serial.print((float)temparr100x[i]/100);
          Serial.print(' ');
        } 
        Serial.println();
      }
      if (cmd == 'D') { //command display
        for(int i=0; i<cmdcnt; i++) {
          Serial.print(i);
          Serial.print(' ');
          Serial.print(cmdarr[i]); 
          Serial.print(' ');
          Serial.print(param1arr[i]); 
          Serial.print(' ');
          Serial.print(param2arr[i]); 
          Serial.print(' ');
          Serial.print(param3arr[i]); 
          Serial.print(' ');
          Serial.print(param4arr[i]); 
          Serial.print(' ');
          Serial.println(param5arr[i]); 
        }
      }
      if (cmd == 'X') { //excute command
         //cyclepointer=0;
         cmdpointer=0;
         curcycle=1;
         cmdtrig=1;
         timearr0=millis()/1000; //reset reference time
      }
    }
  }
}
