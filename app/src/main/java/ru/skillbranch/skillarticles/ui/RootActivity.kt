package ru.skillbranch.skillarticles.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.databinding.LayoutBottombarBinding
import ru.skillbranch.skillarticles.databinding.LayoutSubmenuBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory

class RootActivity : AppCompatActivity() {

    private val viewModel: ArticleViewModel by viewModels { ViewModelFactory("0") }
    private val binding: ActivityRootBinding by viewBinding(ActivityRootBinding::inflate)
    private val bottombarBinding: LayoutBottombarBinding
        get() = binding.bottombar.binding
    private val submenuBinding: LayoutSubmenuBinding
        get() = binding.submenu.binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("asdfasdf", "$binding")

        setupToolbar()
        setupBottombar()
        setupSubmenu()

        viewModel.observeState(this) {
            renderUi(it)
        }
        viewModel.observeNotifications(this) {
            renderNotification(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val menuItem = menu.findItem(R.id.action_search)
        val searchView = (menuItem.actionView as SearchView)

        searchView.isFocusable = true

        if (viewModel.currentState.isSearch) {
            menuItem.expandActionView()
            searchView.setQuery(viewModel.currentState.searchQuery, false)
            searchView.requestFocus()
        } else {
            searchView.clearFocus()
        }

        menuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                return true
            }

        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(binding.container, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombarBinding.root)

        when (notify) {
            is Notify.TextMessage -> {
            }
            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel) {
                    notify.actionHandler.invoke()
                }
            }
            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) {
                        notify.errHandler?.invoke()
                    }
                }
            }
        }

        snackbar.show()
    }

    private fun setupSubmenu() {
        with(submenuBinding) {
            btnTextUp.setOnClickListener { viewModel.handleUpText() }
            btnTextDown.setOnClickListener { viewModel.handleDownText() }
            switchMode.setOnClickListener { viewModel.handleNightMode() }
        }
    }

    private fun setupBottombar() {
        with(bottombarBinding) {
            btnLike.setOnClickListener { viewModel.handleLike() }
            btnBookmark.setOnClickListener { viewModel.handleBookmark() }
            btnShare.setOnClickListener { viewModel.handleShare() }
            btnSettings.setOnClickListener { viewModel.handleToggleMenu() }
        }
    }

    private fun renderUi(data: ArticleState) {
        with(binding) {
            bottombar.binding.btnSettings.isChecked = data.isShowMenu
            if (data.isShowMenu) submenu.open() else submenu.close()

            bottombar.binding.btnLike.isChecked = data.isLike
            bottombar.binding.btnBookmark.isChecked = data.isBookmark

            submenuBinding.switchMode.isChecked = data.isDarkMode
            delegate.localNightMode =
                if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

            if (data.isBigText) {
                tvTextContent.textSize = 14f
                submenuBinding.btnTextUp.isChecked = true
                submenuBinding.btnTextDown.isChecked = false
            } else {
                tvTextContent.textSize = 10f
                submenuBinding.btnTextUp.isChecked = false
                submenuBinding.btnTextDown.isChecked = true
            }

            tvTextContent.text =
                if (data.isLoadingContent) "loading" else data.content.first() as String
            tvTextContent.setOnClickListener {
                startActivity(Intent(this@RootActivity, MainActivity::class.java))
//                this@RootActivity.finish()
            }

            toolbar.title = data.title ?: "loading"
            toolbar.subtitle = data.category ?: "loading"
            if (data.categoryIcon != null) toolbar.logo = getDrawable(data.categoryIcon as Int)
        }
    }

    private fun setupToolbar() {
        with(binding) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            val logo =
                if (toolbar.childCount > 2) toolbar.getChildAt(2) as ImageView else null
            logo?.scaleType = ImageView.ScaleType.CENTER_CROP
            val lp = logo?.layoutParams as? Toolbar.LayoutParams
            lp?.let {
                it.width = this@RootActivity.dpToIntPx(40)
                it.height = this@RootActivity.dpToIntPx(40)
                it.marginEnd = this@RootActivity.dpToIntPx(16)
                logo.layoutParams = it
            }
        }
    }
}
