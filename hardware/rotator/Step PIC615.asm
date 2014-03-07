;*********************************************************************
;
;    Filename:      Step 615 2014-02-28
;    Date:          2014-02-28
;
;    Author:         Rich Harrington
;    Company:        KN6FW
;
;*********************************************************************
;****************** Files required ***********************************
;
;                    12HV615.lkr
;                    P12HV615.INC
;
;*********************************************************************
;
;****************** Notes ********************************************
;       		   10
;       		    ____| |____ GND
;       		   |  + | |
;       	  1.5K	   |	      __  __
;     + Volts  -/\/\/\-----|----+5  1|o \/  |8 Gnd
;       			GP5 2|      |7 GP0 Step 1 red
;       	    blue Step 4	GP4 3|      |6 GP1 Step 2 yellow
;       	     Input only GP3 4|______|5 GP2 Step 3 white
;       
;	Each output needs a transistor drive to provide the 200 mA required
;	Use a darlington to reduce base drive current 2N6426 oe
;				
;			               _________         	
;			   	      |	        |         
;		     _________________|	        |_______ + volts 
;		  | |	         |    | stepper |     |	    
;	 	  | 	         |    | coil    |     | 
;	__________| |<--         |    |_________|     |  
;		  |    |         |   		      |    
;		  | |__| IRZ34   |_______|\|__________|	
;		       |   	         |/|  
;		       | 
;		       |   	       1N4001              
;		    Ground  
;                           
;0         1         2         3  
;012345678901234567890123456789012  Count
; ________                        ________
;|	  |______________________|        |____________ Step 1 on count 0-9
;         ________                        _______ 
;________|        |______________________|       |_____ Step 2 on count 8-17 							 
;	          ________
;________________|        |____________________________ Step 3 on count 16-25 
;	                  ________
;________________________|        |____________________ Step 4 on count 24-1 
;							Reset counter at 32
;
; counter =  0 turn on  Step 1
; counter =  1 turn off Step 4
; counter =  8 turn on  Step 2
; counter =  9 turn off Step 1
; counter = 16 turn on  Step 3
; counter = 17 turn off Step 2
; counter = 24 turn on  Step 4
; counter = 25 turn off Step 3
; Counter = 32 reset to 0
;
;
;*********************************************************************

    #include <P12HV615.inc>   ; processor specific variable definitions
    list      p=12HV615       ; list directive to define processor

    __CONFIG   _WDT_OFF  & _MCLRE_OFF & _IOSCFS_4MHZ & _INTRC_OSC_NOCLKOUT & _PWRTE_ON & _BOR_OFF

;***** VARIABLE DEFINITIONS *******************************************
   
Timer1		EQU                     0x040
;		EQU                     0x041
;		EQU                     0x042
;		EQU                     0x043
;		EQU                     0x044
;		EQU                     0x045
;		EQU                     0x046
;		EQU                     0x047
;		EQU                     0x048
;
;**********************************************************************
;
RESET_VECTOR:                   ; processor reset vector
	goto	Start
INT_VECTOR:                     ; interrupt vector location
        org     4                       
        retfie                          
Start:

        bsf     STATUS,5        ; Bank 1
        movlw   0x08            ; GP 0,1,2,4,5  Outputs
        movwf   TRISIO 
        bcf     STATUS,5        ; Bank 0
; 
Step:
	movlw		0x11
	movwf		GPIO
					; Turn on  1
        call		Timerloop
					; Turn off 4
        movlw		0x01
	movwf		GPIO
        call		Timerloop	;  2
        call		Timerloop	;  3
        call		Timerloop	;  4
        call		Timerloop	;  5
	call		Timerloop	;  6
	call		Timerloop	;  7
        call		Timerloop	;  8
;        
					; Turn on  2
        movlw		0x03
	movwf		GPIO
        call		Timerloop	;  9
					; Turn off 1
        movlw		0x02
	movwf		GPIO
        call		Timerloop	; 10
        call		Timerloop	; 11
        call		Timerloop	; 12
        call		Timerloop	; 13
        call		Timerloop	; 14
	call		Timerloop	; 15
        call		Timerloop	; 16
;        
					; Turn on  3
        movlw		0x06
	movwf		GPIO        
        call		Timerloop	; 17
					; Turn off 2
        movlw		0x04
	movwf		GPIO
        call		Timerloop	; 18
        call		Timerloop	; 19
        call		Timerloop	; 20
        call		Timerloop	; 21
        call		Timerloop	; 22
	call		Timerloop	; 23
        call		Timerloop	; 24
;        
					; Turn on  4
        movlw		0x14
	movwf		GPIO        
        call		Timerloop	; 25
        				; Turn off 3
        movlw		0x10
	movwf		GPIO
        call		Timerloop	; 26
        call		Timerloop	; 27
        call		Timerloop	; 28
        call		Timerloop	; 29
        call		Timerloop	; 30
	call		Timerloop	; 31
        call		Timerloop	; 32
;        
        goto		Step

        
Timerloop:
         nop				; 1
         nop				; 2 6 RPM
;         nop				; 3
;         nop				; 4
;         
        decfsz          Timer1,f
        goto            Timerloop
;
        retlw 0

        END                               ; directive 'end of program'

