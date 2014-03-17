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
#endif

// Function Prototypes
void TxByte(BYTE b);
BYTE RxByte(void);
int IsZeroIndex();

const BYTE ZERO_INDEX = 0xF0;
const BYTE MIN_BPR = 100;     //Minimum bytes per rotation
BOOL zeroIndexFlag = MIN_BPR;

int main(void)
{
    BYTE  b;

    mPORTASetPinsDigitalOut();      //Set Attinuator Port Direction
    mPORTAWrite(0);
    mPORTBSetPinsDigitalIn(BIT_14); //Set ZeroIndex Pin


    //Assign UART Programmable Pins
    PPSInput(2,U1RX,RPB8);
    PPSOutput(4,RPB9,U1TX);
    PPSInput(2,URX,RPB8);
    PPSOutput(4,RPB9,U2TX);

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

    int c = MIN_BPR; //counter used to ensure only one zeroIndex per rotation;

    while(1)
    {
        if (mPORTBReadBits(BIT_14) && c <= 0) {  //one index per rotation
            zeroIndexFlag = TRUE;
            c = MIN_BPR;
        }
        if (!UARTReceivedDataIsAvailable(UART_TABLET)) handleTabletInput;
        
        b = PassRadioByte();
        if (zeroIndexFlag && b == 0x10) handleIndex();
        if (c > 0) c--;
    }

    return -1;
}


// *****************************************************************************
// void TxByte(UINT32 byte)
// *****************************************************************************
void TxRadioByte(BYTE b)
{
    while(!UARTTransmitterIsReady(UART_TABLET));

    UARTSendDataByte(UART_TABLET, b);

    while(!UARTTransmissionHasCompleted(UART_TABLET));
}

// *****************************************************************************
// BYTE RxByte(void)
// *****************************************************************************
BYTE RxRadioByte(void)
{
    while(!UARTReceivedDataIsAvailable(UART_RADIO)); //idle loop

    return UARTGetDataByte(UART_RADIO);
}

BYTE PassRadioByte(BYTE b)
{
    b = RxByte();
    TxByte(b);
    return b;
}

// *****************************************************************************
// void SetAttenuation(BYTE)
// *****************************************************************************
int SetAttenuation(BYTE b)
{
    return mPORTAWrite(b);
}

// *****************************************************************************
// void PressButton(BYTE)
// *****************************************************************************
void PressButton(BYTE b)
{
    int c = 16; //one cycle
    while(!UARTTransmitterIsReady(UART_RADIO));
    while((PassRadioByte || PassRadioByte) && c) { c--; }; //until 00 00 or c=0

    //todo: pause 660us
    
    UARTSendDataByte(UART_RADIO, b);

    while(!UARTTransmissionHasCompleted(UART_RADIO));
}

void handleIndex()
{
    BYTE header_byte = RxByte();
    if (header_byte == 0xFF)
    {
        TxByte(ZERO_INDEX);
        zeroIndexFlag = FALSE;
    }
    else
    {
        TxByte(header_byte);
    }
}

void handleTabletInput() {

    switch(UARTGetDataByte(UART_TABLET))
    {
        case 'A':
            if (!UARTReceivedDataIsAvailable(UART_RADIO))
            {
                SetAttenuation(UARTGetDataByte(UART_TABLET));
            }
            break;
        case 'B':
            if (!UARTReceivedDataIsAvailable(UART_RADIO))
            {
                PressButton(UARTGetDataByte(UART_TABLET));
            }
            break;
    }

    while (!UARTReceivedDataIsAvailable(UART_RADIO)) { UARTGetDataByte(UART_TABLET); }
}
