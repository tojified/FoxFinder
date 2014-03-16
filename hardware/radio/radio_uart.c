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

#define UART_MODULE_ID UART2 // PIM is connected through UART2 module
#endif

// Function Prototypes
void TxByte(BYTE b);
BYTE RxByte(void);
int IsZeroIndex();

const BYTE ZERO_INDEX = 0xF0;

int main(void)
{
    BYTE  b;

    #if defined (__32MX250F128B__)
    PPSInput(2,U2RX,RPB5); // Assign RPB5 as input pin for U2RX
    PPSOutput(4,RPB0,U2TX); // Set RPB0 pin as output for U2TX
    //#elif defined (__32MX430F064L__) || (__32MX450F256L__) || (__32MX470F512L__)
    //PPSInput(2,U1RX,RPF4); // Assign RPF4 as input pin for U1RX
    //PPSOutput(2,RPF5,U1TX); // Set RPF5 pin as output for U1TX
    #endif

    UARTConfigure(UART_MODULE_ID, UART_ENABLE_PINS_TX_RX_ONLY);
    UARTSetFifoMode(UART_MODULE_ID, UART_INTERRUPT_ON_TX_NOT_FULL | UART_INTERRUPT_ON_RX_NOT_EMPTY);
    UARTSetLineControl(UART_MODULE_ID, UART_DATA_SIZE_8_BITS | UART_PARITY_NONE | UART_STOP_BITS_1);
    UARTSetDataRate(UART_MODULE_ID, GetPeripheralClock(), 9600);
    UARTEnable(UART_MODULE_ID, UART_ENABLE_FLAGS(UART_PERIPHERAL | UART_RX | UART_TX));

    while(1)
    {
        b = RxByte();

        switch(b)
        {
        case 0x10:
            BYTE header_byte = RxByte();
            if (header_byte == 0xFF && IsZeroIndex())
            {
                TxByte(ZERO_INDEX);
            }
            else
            {
                TxByte(header_byte);
            }
            break;

        default:
            TxByte(b);
        }
    }

    return -1;
}


// *****************************************************************************
// void TxByte(UINT32 byte)
// *****************************************************************************
void TxByte(BYTE b)
{
    while(!UARTTransmitterIsReady(UART_MODULE_ID));

    UARTSendDataByte(UART_MODULE_ID, b);

    while(!UARTTransmissionHasCompleted(UART_MODULE_ID));
}

// *****************************************************************************
// UINT32 GetByte(void)
// *****************************************************************************
BYTE RxByte(void)
{
    BYTE  b;

    while(!UARTReceivedDataIsAvailable(UART_MODULE_ID)); //idle loop

    b = UARTGetDataByte(UART_MODULE_ID);
    b -= '0';

    return b;
}

// *****************************************************************************
// UINT32 IsIndex(void)
// *****************************************************************************
int IsZeroIndex()
{
    //todo: check if pin designated to zero index is high
    return 1;
}
