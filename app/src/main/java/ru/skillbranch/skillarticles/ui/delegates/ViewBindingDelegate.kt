package ru.skillbranch.skillarticles.ui.delegates

import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ViewBindingDelegate<T : ViewBinding>(
    private val activity: AppCompatActivity,
    private val initializer: (LayoutInflater) -> T
) : ReadOnlyProperty<AppCompatActivity, T>, LifecycleObserver {

    private var viewBinding: T? = null

    init {
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        Log.i("asdfasdf", "create")
        if (viewBinding == null) {
            viewBinding = initializer(activity.layoutInflater)
        }

        activity.setContentView(viewBinding!!.root)
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    fun onDestroy() {
//        Log.i("asdfasdf", "destroy")
//        viewBinding = null
//
//        activity.lifecycle.removeObserver(this)
//    }

    override fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): T {
        if (viewBinding == null && thisRef.lifecycle.currentState != Lifecycle.State.DESTROYED) {
            viewBinding = initializer(thisRef.layoutInflater)
        }

        return viewBinding!!
    }
}

inline fun <reified T : ViewBinding> AppCompatActivity.viewBinding(noinline initializer: (LayoutInflater) -> T) =
    ViewBindingDelegate(this, initializer)
