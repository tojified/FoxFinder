/*********************************************************************
 *
 *      PIC32MX UART Radio Controller for FoxFinder
 *
 *********************************************************************
 * FileName:        radio_uart.c
 * Dependencies:    plib.h
 *
 * Processor:       PIC32MX
 *
 * Complier:        MPLAB XC32
 *                  MPLAB X IDE
 *
 *********************************************************************
 * The purpose of this routine is to emulate the Yeasu radio mic.
 *
 * Platform: FoxFinder Android application.
 *
 ********************************************************************/
#include <plib.h>

#if defined (__32MX250F128B__)
// Configuration Bit settings
// SYSCLK = 48 MHz (8MHz Crystal / FPLLIDIV * FPLLMUL / FPLLODIV)
// PBCLK = 48 MHz (SYSCLK / FPBDIV)
// Primary Osc w/PLL (XT+,HS+,EC+PLL)
// WDT OFF
// Other options are don't care
#pragma config FPLLMUL = MUL_24, FPLLIDIV = DIV_2, FPLLODIV = DIV_2, FWDTEN = OFF
#pragma config POSCMOD = HS, FNOSC = PRIPLL, FPBDIV = DIV_1
#define SYS_FREQ (48000000L)
#endif

#define	GetPeripheralClock()		(SYS_FREQ/(1 << OSCCONbits.PBDIV))
#define	GetInstructionClock()		(SYS_FREQ)


#define UART_TABLET UART1 // Tablet is connected through UART1 module
#define UART_RADIO  UART2 // Radio is connected through UART2 module

// Function Prototypes
BYTE ReadRadio(void);
void WriteTablet(BYTE b);
BYTE PassData(void);
BOOL IsDoubleZero(void);

BOOL IsIndex(void);
void SetAttenuation(BYTE b);
void SendButtonPress(BYTE b);

void ReadTablet(void);
void WriteIndex(void);

const BYTE MIN_BPR = 500;   //Minimum bytes per rotation
int indexOff = MIN_BPR;     //decremented counter ensurse only one zeroIndex;
BOOL zeroIndexFlag = FALSE;

BYTE TabletCommand = 0;

int main(void)
{
    mPORTASetPinsDigitalOut();      //Set Attinuator Port Direction
    mPORTAWrite(0);
    mPORTBSetPinsDigitalIn(BIT_14); //Set ZeroIndex (RB14 - Pin 25)

    //Assign UART Programmable Pins
    PPSInput(3,U1RX,RPB13);  //Pin 24
    PPSOutput(1,RPB15,U1TX); //Pin 26
    PPSInput(2,U2RX,RPB8);   //Pin 17 (+5v tollerant)
    PPSOutput(4,RPB9,U2TX);  //Pin 18 (+5v tollerant)

    UARTConfigure(UART_TABLET, UART_ENABLE_PINS_TX_RX_ONLY);
    UARTSetFifoMode(UART_TABLET, UART_INTERRUPT_ON_TX_NOT_FULL | UART_INTERRUPT_ON_RX_NOT_EMPTY);
    UARTSetLineControl(UART_TABLET, UART_DATA_SIZE_8_BITS | UART_PARITY_NONE | UART_STOP_BITS_1);
    UARTSetDataRate(UART_TABLET, GetPeripheralClock(), 9600);
    UARTEnable(UART_TABLET, UART_ENABLE_FLAGS(UART_PERIPHERAL | UART_RX | UART_TX));

    UARTConfigure(UART_RADIO, UART_ENABLE_PINS_TX_RX_ONLY);
    UARTSetFifoMode(UART_RADIO, UART_INTERRUPT_ON_TX_NOT_FULL | UART_INTERRUPT_ON_RX_NOT_EMPTY);
    UARTSetLineControl(UART_RADIO, UART_DATA_SIZE_8_BITS | UART_PARITY_NONE | UART_STOP_BITS_1);
    UARTSetDataRate(UART_RADIO, GetPeripheralClock(), 9600);
    UARTEnable(UART_RADIO, UART_ENABLE_FLAGS(UART_PERIPHERAL | UART_RX | UART_TX));

    while(1)
    {
        if (!UARTReceivedDataIsAvailable(UART_TABLET)) ReadTablet();
        
        if (IsIndex())
        {
            zeroIndexFlag = TRUE;
            indexOff = MIN_BPR;
        }

        if (zeroIndexFlag && PassData() == 0x10) WriteIndex(); //Passess
    }

    return -1;
}

// *****************************************************************************
// void WriteTablet(BYTE byte)
// *****************************************************************************
void WriteTablet(BYTE b)
{
    while(!UARTTransmitterIsReady(UART_TABLET));

    UARTSendDataByte(UART_TABLET, b);

    while(!UARTTransmissionHasCompleted(UART_TABLET));
}

// *****************************************************************************
// BYTE ReadRadio(void)
// *****************************************************************************
BYTE ReadRadio(void)
{
    while(!UARTReceivedDataIsAvailable(UART_RADIO)); //idle loop - block until data

    return UARTGetDataByte(UART_RADIO);
}

// *****************************************************************************
// BYTE PassData(void)
// *****************************************************************************
BYTE PassData(void)
{
    BYTE b = ReadRadio();
    WriteTablet(b);
    return b;
}

// *****************************************************************************
// BOOL IsDoubleZero(void)
// *****************************************************************************
BOOL IsDoubleZero(void)
{
    PassData == 0 && PassData == 0;
}

// *****************************************************************************
// BOOL IsIndex(void)
// *****************************************************************************
BOOL IsIndex(void)
{
    if (indexOff > 0)
    {
        indexOff--;
        return FALSE;
    }
    else
    {
        return mPORTBReadBits(BIT_14);
    }
}

// *****************************************************************************
// void SetAttenuation(BYTE)
// *****************************************************************************
void SetAttenuation(BYTE b)
{
    mPORTAWrite(b);
}

// *****************************************************************************
// void SendButtonPress(BYTE)
// *****************************************************************************
void SendButtonPress(BYTE b)
{
    while(!UARTTransmitterIsReady(UART_RADIO)); //transmitter ready
    while(!IsDoubleZero())                      //block until 00 00

    Delay(600);  //wait atleast 600us
    
    UARTSendDataByte(UART_RADIO, b);

    while(!UARTTransmissionHasCompleted(UART_RADIO));
}

// *****************************************************************************
// void ReadTablet(void)
// *****************************************************************************
void ReadTablet(void) {

    if (TabletCommand)
    {
        switch(TabletCommand)
        {
            case 'A':
                SetAttenuation(UARTGetDataByte(UART_TABLET));
                break;
            case 'B':
                SendButtonPress(UARTGetDataByte(UART_TABLET));
                break;
        }
        TabletCommand = 0;
    }
    else
    {
        TabletCommand = UARTGetDataByte(UART_TABLET);
    }
}

// *****************************************************************************
// void WriteIndex(void)
// *****************************************************************************
void WriteIndex(void)
{
    BYTE header_byte = ReadRadio();
    if (header_byte == 0xFF)
    {
        WriteTablet(0xF0);
        zeroIndexFlag = FALSE;
    }
    else
    {
        WriteTablet(header_byte);
    }
}

void Delay(DWORD dwCount)
{
    UINT countPerMicroSec = GetPeripheralClock()/100;
    UINT backupCount;
    backupCount = ReadCoreTimer();
    dwCount *= countPerMicroSec;
    while( (ReadCoreTimer() - backupCount) < dwCount );
}
