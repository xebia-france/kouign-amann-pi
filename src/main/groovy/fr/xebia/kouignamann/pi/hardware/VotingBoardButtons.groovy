package fr.xebia.kouignamann.pi.hardware

import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.RaspiPin
import com.pi4j.io.i2c.I2CDevice
import fr.xebia.kouignamann.pi.hardware.plate.MCP23017
import org.vertx.java.core.logging.Logger

/**
 * Created by amaury on 23/01/2014.
 */
class VotingBoardButtons {

    final Logger log

    final GpioController gpio

    final I2CDevice i2cDevice

    final Map<String, GpioPinDigitalOutput> buttons

    VotingBoardButtons(GpioController gpio, I2CDevice i2cDevice, Logger log) {
        this.log = log
        this.gpio = gpio
        this.i2cDevice = i2cDevice

        // TODO: Change values to reflect Select, up, down, left, right, according to PCB schema
        buttons = [
                'Button1': gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, 'Button1', PinState.LOW), // GPIO # 18
                'Button2': gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, 'Button2', PinState.LOW), // GPIO # 22
                'Button3': gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, 'Button3', PinState.LOW), // GPIO # 23
                'Button4': gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, 'Button4', PinState.LOW),// GPIO # 24
                'Button5': gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, 'Button5', PinState.LOW) // GPIO # 25
        ]

        lightOnAll()

        sleep 1000

        lightOffAll()
    }

    def lightOnAll() {
        log.info('lightOnAll')
        for (i in 1..5) {
            buttons["Button${i}"]?.high()
        }

    }

    def lightOffAll() {
        log.info('lightOffAll')
        for (i in 1..5) {
            buttons["Button${i}"]?.low()
        }

    }

    def lightOnOneButton(int note) {
        log.info('lightOnOneButton: ' + note)

        for (i in 1..5) {
            if (i != note) {
                buttons["Button${i}"].low()
            }
        }
        sleep 1000
        buttons["Button${note}"].low()


    }

    List<Integer> readButtonsPressed() {

        List<Integer> pressed = []

        buttons.size().times { index ->
            pressed << digitalRead(index)
        }

        pressed
    }

    private Integer digitalRead(Integer pinByte) {

        if (pinByte > 15) {
            // only 16 bits!
            return 0
        }

        byte gpioaddr = (pinByte < 8 ? MCP23017.GPIOA : MCP23017.GPIOB)

        // read the current GPIO
        i2cDevice.write(gpioaddr)

        (byte) (reada() >> (pinByte % 8)) & (byte) 0x1
    }

    private byte reada() {
        reada(1)[0]
    }

    private byte[] reada(int len) {
        byte[] b = new byte[len]
        i2cDevice.read(b, 0, len)
        return b
    }

    void stop() {
        lightOffAll()
        gpio.shutdown();
    }
}
