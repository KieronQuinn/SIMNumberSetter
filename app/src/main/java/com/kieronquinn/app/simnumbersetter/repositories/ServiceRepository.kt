package com.kieronquinn.app.simnumbersetter.repositories

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.kieronquinn.app.simnumbersetter.IPhoneNumberSetter
import com.kieronquinn.app.simnumbersetter.utils.extensions.applySecurity
import com.kieronquinn.app.simnumbersetter.utils.extensions.suspendCoroutineWithTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

interface ServiceRepository {

    suspend fun <T> runWithService(block: suspend (IPhoneNumberSetter) -> T): T?
    fun <T> runWithServiceIfAvailable(block: (IPhoneNumberSetter) -> T?): T?
    fun unbindServiceIfNeeded()

}

class ServiceRepositoryImpl(private val context: Context): ServiceRepository {

    companion object {
        private val SERVICE_TIMEOUT = TimeUnit.SECONDS.toMillis(10)
    }

    private var serviceInstance: IPhoneNumberSetter? = null
    private var serviceConnection: ServiceConnection? = null
    private val getServiceMutex = Mutex()

    private val serviceIntent = Intent("com.android.phone.TelephonyDebugService").apply {
        `package` = "com.android.phone"
        applySecurity(context)
    }

    override suspend fun <T> runWithService(block: suspend (IPhoneNumberSetter) -> T): T? {
        val service = withContext(Dispatchers.IO) {
            getServiceLocked()
        } ?: return null
        return block(service)
    }

    override fun <T> runWithServiceIfAvailable(block: (IPhoneNumberSetter) -> T?): T? {
        return serviceInstance?.let {
            block(it)
        }
    }

    override fun unbindServiceIfNeeded() {
        serviceConnection?.let {
            try {
                context.unbindService(it)
            }catch (e: IllegalArgumentException){
                //Already unregistered
            }
        }
    }

    private suspend fun getServiceLocked() = suspendCoroutineWithTimeout<IPhoneNumberSetter>(SERVICE_TIMEOUT) { resume ->
        runBlocking {
            getServiceMutex.lock()
            serviceInstance?.let {
                resume.resume(it)
                getServiceMutex.unlock()
                return@runBlocking
            }
            val serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(component: ComponentName, binder: IBinder) {
                    serviceInstance = IPhoneNumberSetter.Stub.asInterface(binder)
                    binder.pingBinder()
                    serviceConnection = this
                    resume.resume(serviceInstance!!)
                    getServiceMutex.unlock()
                }

                override fun onServiceDisconnected(component: ComponentName) {
                    serviceInstance = null
                    serviceConnection = null
                }
            }
            withContext(Dispatchers.Main) {
                context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

}