;*********************************************************************
;
V_number	EQU	10
;
;    Author:         Rich Harrington
;    Company:        KN6FW
;
;****************** Files required ***********************************
;
;                    P16f690.lkr
;                    P16f690.inc
;
;*********************************************************************
;****************** Notes ********************************************
;
;				      __  __
;				+5  1|o \/  |20 Ground
;		External Clock	RA5 2|      |19 RA0 Index
;		NC		RA4 3|      |18 RA1 Display
;		NC		RA3 4|      |17 RA2
;		Test Bit 5	RC5 5|      |16 RC0 Test Bit 0
;		Test Bit 4	RC4 6|      |15 RC1 Test Bit 1
;		Test Bit 3	RC3 7|      |14 RC2 Test Bit 2
;		Test Bit 6	RC6 8|      |13 RB4 NC
;		Test Bit 7	RC7 9|      |12 RB5 UART in
;		UART out	RB710|______|11 RB6 Send Button Now
;
;
;*********************************************************************
;
    #include <P16f690.inc>   ; processor specific variable definitions
    list      p=16f690       ; list directive to define processor

    errorlevel  -302         ; suppress message 302 from list file
    __CONFIG   _WDT_OFF  & _MCLRE_OFF & _HS_OSC & _PWRTE_ON & _BOR_OFF
;    __CONFIG   _WDT_OFF  & _MCLRE_OFF & _EC_OSC & _PWRTE_ON & _BOR_OFF

;***** VARIABLE DEFINITIONS *******************************************
;
UART_flags			EQU	0x030
rxdata				EQU	0x031
test2				EQU	0x032
test1				EQU	0x033
;				EQU	0x034
;				EQU	0x035
;				EQU	0x036
;				EQU	0x037
;				EQU	0x038
;				EQU	0x039
;				EQU	0x03A
;				EQU	0x03B
;				EQU	0x03C
;				EQU	0x03E
;				EQU	0x03F
;
; All Banks
;
;				EQU	0x07b
Disp_Stor			EQU	0x07c
Index:				EQU	0x07d
Wret:				EQU	0x07d
W_Temp				EQU	0x07e
STATUS_Temp			EQU	0x07f
;
;******** Values ******************************************
;
Data_Port:			EQU	PORTA
Data_Num:			EQU	0
Index_port:			EQU	PORTA
Index_Num			EQU	1
;
;**********************************************************
;
;****** Flag Bits *****************************************
;
;
;
;**********************************************************************
;
;RESET_VECTOR:                   	; processor reset vector
	org	0
	goto   Start			; go to beginning of program
;
;INT_VECTOR:
; Interrupt from TIMER0
; "Timer_ran" runs all the time and is used to randonize PAD number
;
; Timers do nothing in this code.
; The timers set a flag in "Timer_Flags" when the timer get to Zero
; Program code Has to set timers for timers to Run!

	org     4
Timer0_int:
	movwf	W_Temp			; Save State
	swapf	STATUS,W
	clrf	STATUS			; Bank 0, Clears IRP,RP1,RP0
	movwf	STATUS_Temp
	btfss   INTCON,2
	goto	Timer_exit
	bcf     INTCON,2        	; reset timer0 int
Timer_exit:
;
Start:
;
;	Init stuff
;
	bsf     STATUS, RP0		; Bank 1 to set Port Direction
	movlw	0x01			; Bit 0 Input
	movwf 	TRISA
	movlw 	0x020			; Set to Port B 1-2 UART
	movwf 	TRISB
	movlw 	0x00			; Set to Port C All Output
	movwf 	TRISC

	movlw	0x0c1			; Set timer to div 4
	movwf	OPTION_REG
	movlw	0x9f			; 9600 Baud
	movwf	SPBRG
	movlw	0x24			; UART TX
	movwf	TXSTA

	movlw	0x08			; UART Baud Rate Control
	movwf	BAUDCTL
	bcf     STATUS, RP0		; Bank 0
	movlw	0x080			; UART RX
	movwf	RCSTA
;
	movlw	0x0aa
	movwf	PORTC
	movlw	'F'
	movwf	TXREG

Do_it_loop:
;	movlw	0x080			; UART RX
;	movwf	RCSTA
	btfss	PIR1,RCIF
	goto	UART_not_RX
	movf	PIR1,W			; Get UART Flags
	movwf	UART_flags
	comf	UART_flags,w
	movwf	PORTC

	movf	RCREG,W			; Get RX data
	movwf	TXREG			; Send it
	movwf	rxdata
	comf	rxdata,w
;	movwf	PORTC			; Display it
;	movlw	0x055
;	movwf	PORTC
UART_not_RX:
	goto	Do_it_loop
Display:
                                        ; Subroutine to Display
                                        ; an 8 bit reg
                                        ; w = reg to display
                                        ; Data_Port
                                        ; Data_Num
                                        ; Disp_Stor Memory Location
                                        ; Index     Memory Location
	movwf	Wret
	movwf   Disp_Stor
	movlw   8                       ; Do this for 8 bits
	movwf   Index                   ; Shift reg clock pos
	bsf     Data_Port,Data_Num      ; Start with clock high

Display_loop:
	rlf     Disp_Stor,f
	btfsc   STATUS,0                ; Carry flag
	goto    Itsa1
	                                ; Itsa0
	bcf     Data_Port,Data_Num
	nop
	nop
	nop
	nop
	nop
	nop
	nop
	nop
	nop
	bsf     Data_Port,Data_Num
	goto    Dlooptest
Itsa1:
	nop
	nop
	nop
	nop
	nop
	nop
	nop
	nop
	nop
	bcf     Data_Port,Data_Num
	bsf     Data_Port,Data_Num
Dlooptest:
	decfsz  Index,f
	goto    Display_loop
;
	movf	Wret,W
	return
;
;**********************************************************
;
;
;   UART
;
;	TXREG Transmit Data Reg
;
;	RCREG Receive Data Reg
;
;	TRISB Input = 1  Output = 0  UART over wrights  I Think  Page 70
;
;	PIE1 - PIE2 Peripheral Interrupt Reg  Not using Interrupts
;
;   ***	PIR1 **** Bank 1 ****
;   *
;   *	Bit  4	    TXIF UART TX Flag = 1 Ready for TX Char
;   *	Bit  5	    RCIF UART RX Flag = 1 Send RX Char
;
;	TXSTA Transmit Status and Control Reg 0x24  Page 160
;
;       Bit  0     TX9D 9 Bit TX =?
;       Bit  1     TRMT TSR Empty = 1 Full = 0
;       Bit  2     BRGH High Baud rate select = 1
;       Bit  3     SENDB Send Break = 1
;       Bit  4     SYNC Asynchronous = 0
;       Bit  5     TXEN Enable TX = 1
;       Bit  6     TX9 8 bit TX = 0
;       Bit  7     CSRC Sync Mode = X
;
;	RCSTA Receive Status and Control Reg 0x90  Page 161
;
;       Bit  0     RX9D 9 Bit RX = ?
;       Bit  1     OERR Overrun error = 1
;       Bit  2     FERR Framing error = 1
;       Bit  3     ADDEN Asynchronous 8 Bit = X
;       Bit  4     CREN Asynchronous Enable RX = 1
;       Bit  5     SREN Single RX Sync = X
;       Bit  6     RX9 8 bit RX = 0
;       Bit  7     SPEN Serial Enable = 1
;
;	BAUDCTL Baud Rate Control Reg 0x90  Page 162
;
;       Bit  0     ABDEN Auto Baud Detect = 0
;       Bit  1     WUE Wake-up Enable = S
;       Bit  2     X
;       Bit  3     BRG16 16 Bit Baud Rate Gen = 1
;       Bit  4     SCKP Sync Clock = X
;       Bit  5     X
;       Bit  6     RCIDL Receive Idle Flag Bit 1 = Idle
;       Bit  7     ABDOVF Auto Baud Detect Overflow Bit = S
;
;	SPBRG  Bank 1 0xbf  9600
;
;	SPBRGH Bank 1 0x00
;
;**********************************************************************
;
;  Intcon
;
;       Bit  0     int flag
;       Bit  1     Ext int flag
;       Bit  2     Timer0 int flag
;       Bit  3     int enable
;       Bit  4     Ext int enable
;       Bit  5     Timer0 int enable
;       Bit  6     Peripheral int enable
;       Bit  7     Global int enable
;
;       Bit 7
;        GIE   PEIE  T0IE  INTE  GPIE  T0IF INTF GPIF
;         1     1     1     0     0     R    R    R
;
;**********************************************************************
;
;
;**********************************************************************
;
;       ANCON1 Reg = bits 0-3,7 nc
;                    bits 4-6 Clock Rate
;
;       Bit  7       Not Used
;       Bits 654     000 = Fosc/2
;                    001 = Fosc/8
;                    010 = Fosc/32
;                    011 = Fosc External
;                    100 = Fosc/4
;                    101 = Fosc/16
;                    110 = Fosc/64
;                    111 = Reserved
;
;       Bits 3210    Not used
;
;
;       Bit 7
;         -   ADCS2 ADCS1 ADCS0   -     -     -     -
;         0     1     1     0     0     0     0     0      60 Hex
;
;
;**********************************************************************
;
;       ADCON0 Reg =
;
;       Bit    0    Enable = 1
;       Bit    1    Status & start = 1
;       Bits 5432   0000 = Channel 0        Mux
;                   0001 = Channel 1
;                   0010 = Channel 2
;                   0011 = Channel 3
;                   0100 = Channel 4
;                   0101 = Channel 5
;                   0110 = Channel 6
;                   0111 = Channel 7
;		    1000 = Channel 8
;                   1001 = Channel 9
;                   1010 = Channel 10
;                   1011 = Channel 11
;                   1100 = CVref
;                   1101 = 0.6 ref
;                   1110 = Reserved
;                   1111 = Reserved
;       Bit    6    Ref 0 = Vdd
;       Bit    7    Output format MSB left = 0
;
;       Bit 7
;       ADFM  VCFG  CHS3  CHS2  CHS1  CHS0 Go/Done ADON
;         0     0     1     0     1     1     0     1      2d Hex
;
;       Bit 1 = 1 To start conversion.  Bit 1 = 0 When done
;
;**********************************************************************
;
        END                             ; directive 'end of program'
































































