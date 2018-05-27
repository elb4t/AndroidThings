package es.elb4t.androidthings

import android.app.Activity
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManager
import java.io.IOException


/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class MainActivity : Activity() {
    private val BOTON_PIN = "BCM21" // Puerto GPIO del botón
    private lateinit var botonGpio: Gpio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val manager = PeripheralManager.getInstance()

        try {
            botonGpio = manager.openGpio(BOTON_PIN) // 1. Crea conecxión GPIO
            botonGpio.setDirection(Gpio.DIRECTION_IN)// 2. Es entrada
            botonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING) // 3. Habilita eventos de disparo por flanco de bajada
            botonGpio.registerGpioCallback(callback) // 4. Registra callback
        } catch (e: IOException) {
            Log.e(TAG, "Error en PeripheralIO API", e)
        }
    }

    private val callback: GpioCallback = GpioCallback { gpio ->
        try {
            Log.e(TAG, "cambio botón ${gpio.value}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        true // 5. devolvemos true para mantener callback activo
    }

    override fun onDestroy() {
        super.onDestroy()
        if (botonGpio != null) { // 6. Cerramos recursos
            botonGpio.unregisterGpioCallback(callback)
            try {
                botonGpio.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error en PeripheralIO API", e)
            }

        }
    }
}
