package es.elb4t.androidthings

import android.app.Activity
import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManager
import com.google.android.things.pio.Pwm
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

    private val INTERVALO_LED = 1000 // Intervalo parpadeo (ms)
    private val LED_PIN = "BCM6" // Puerto GPIO del LED
    private val handler = Handler() // Handler para el parpadeo
    private lateinit var ledGpio: Gpio

    private var PORCENTAGE_LED_PWM = 0.0 // % encendido
    private val LED_PWM_PIN = "PWM0" // Puerto del LED
    private var ledPwm: Pwm? = null

    private val R_LED_PIN = "BCM13" // Puerto GPIO del LED
    private val G_LED_PIN = "BCM19" // Puerto GPIO del LED
    private val B_LED_PIN = "BCM26" // Puerto GPIO del LED
    private lateinit var RledGpio: Gpio
    private lateinit var GledGpio: Gpio
    private lateinit var BledGpio: Gpio
    private var contadorRGB = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val manager = PeripheralManager.getInstance()

        try {
//            botonGpio = manager.openGpio(BOTON_PIN) // 1. Crea conecxión GPIO
//            botonGpio.setDirection(Gpio.DIRECTION_IN)// 2. Es entrada
//            botonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING) // 3. Habilita eventos de disparo por flanco de bajada
//            botonGpio.registerGpioCallback(callback) // 4. Registra callback
//
//            ledGpio = manager.openGpio(LED_PIN) // 1. Crea conexión GPIO
//            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW) // 2. Se indica que es de salida
//
//            ledPwm = manager.openPwm(LED_PWM_PIN); // 1. Crea conexión GPIO
//            ledPwm?.setPwmFrequencyHz(120.0) // 2. Configuramos PWM
//            ledPwm?.setPwmDutyCycle(PORCENTAGE_LED_PWM)
//            ledPwm?.setEnabled(true)
//            handler.post(runnableAzul) // 3. Llamamos al handler

            RledGpio = manager.openGpio(R_LED_PIN) // 1. Crea conexión GPIO
            RledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW) // 2. Se indica que es de salida
            GledGpio = manager.openGpio(G_LED_PIN) // 1. Crea conexión GPIO
            GledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW) // 2. Se indica que es de salida
            BledGpio = manager.openGpio(B_LED_PIN) // 1. Crea conexión GPIO
            BledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW) // 2. Se indica que es de salida
            handler.post(runnableRGB) // 3. Llamamos al handler
        } catch (e: IOException) {
            Log.e(TAG, "Error en PeripheralIO API", e)
        }
    }

    private val callback: GpioCallback = GpioCallback { gpio ->
        try {
            Log.e(TAG, "cambio botón ${gpio.value}")
            if (!gpio.value)
                handler.post(runnable) // 3. Llamamos al handler
            else
                handler.removeCallbacks(runnable)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        true // 5. devolvemos true para mantener callback activo
    }

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            try {
                Log.e(TAG, "Enciende led runnable")
                ledGpio.value = !ledGpio.value // 4. Cambiamos valor LED
                handler.postDelayed(this, INTERVALO_LED.toLong()) // 5. Programamos siguiente llamada dentro de INTERVALO_LED ms
            } catch (e: IOException) {
                Log.e(TAG, "Error en PeripheralIO API", e)
            }
        }
    }
    private val runnableAzul: Runnable = object : Runnable {
        override fun run() {
            try {
                Log.e(TAG, "Enciende led Azul runnable $PORCENTAGE_LED_PWM %")
                ledPwm?.setPwmDutyCycle(PORCENTAGE_LED_PWM) // 4. Cambiamos valor LED
                if (PORCENTAGE_LED_PWM >= 100.0) {
                    PORCENTAGE_LED_PWM = 0.0
                } else {
                    PORCENTAGE_LED_PWM += 20.0
                }
                handler.postDelayed(this, INTERVALO_LED.toLong()) // 5. Programamos siguiente llamada dentro de INTERVALO_LED ms
            } catch (e: IOException) {
                Log.e(TAG, "Error en PeripheralIO API", e)
            }
        }
    }
    private val runnableRGB: Runnable = object : Runnable {
        override fun run() {
            try {
                Log.i(TAG, "Enciende led RGB runnable $contadorRGB")
                when(contadorRGB){
                    0 -> ledRGB(false,false,false)  // Apagados
                    1 -> ledRGB(true,false,false)   // R
                    2 -> ledRGB(false,true,false)   //  G
                    3 -> ledRGB(false,false,true)   //   B
                    4 -> ledRGB(true,true,false)    // RG
                    5 -> ledRGB(false,true,true)    //  GB
                    6 -> ledRGB(true,false,true)    // R B
                    7 -> ledRGB(true,true,true)     // RGB
                }
                if (contadorRGB < 7)
                    contadorRGB ++
                else
                    contadorRGB = 0
                handler.postDelayed(this, INTERVALO_LED.toLong()) // 5. Programamos siguiente llamada dentro de INTERVALO_LED ms
            } catch (e: IOException) {
                Log.e(TAG, "Error en PeripheralIO API", e)
            }
        }
    }

    private fun ledRGB(r: Boolean, g: Boolean, b: Boolean) {
        Log.i(TAG,"Red:${RledGpio.value}, Green: ${GledGpio.value}, Blue: ${BledGpio.value}")
        RledGpio.value = r
        GledGpio.value = g
        BledGpio.value = b
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
        if (ledPwm != null) { // 3. Cerramos recursos
            try {
                ledPwm?.close()
                ledPwm = null
            } catch (e: IOException) {
                Log.e(TAG, "Error al cerrar PWM", e);
            }
        }
    }
}
