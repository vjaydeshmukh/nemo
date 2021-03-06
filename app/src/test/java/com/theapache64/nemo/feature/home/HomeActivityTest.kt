package com.theapache64.nemo.feature.home

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaSleepInteractions
import com.schibsted.spain.barista.interaction.BaristaViewPagerInteractions.swipeViewPagerForward
import com.theapache64.nemo.FakeBannerDataStore
import com.theapache64.nemo.FakeCategoryDataStore
import com.theapache64.nemo.R
import com.theapache64.nemo.data.remote.NemoApi
import com.theapache64.nemo.di.module.ApiModule
import com.theapache64.nemo.feature.cart.CartActivity
import com.theapache64.nemo.feature.productdetail.ProductDetailActivity
import com.theapache64.nemo.feature.products.ProductsActivity
import com.theapache64.nemo.utils.test.IdlingRule
import com.theapache64.nemo.utils.test.MainCoroutineRule
import com.theapache64.nemo.utils.test.monitorActivity
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by theapache64 : Nov 22 Sun,2020 @ 10:30
 */
@UninstallModules(ApiModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeActivityTest {

    @get:Rule
    val idlingRule = IdlingRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val nemoApi: NemoApi = mock()

    @Test
    fun givenBanners_whenGoodBanners_thenBannerDisplayed() {

        // Fake nemo api
        whenever(nemoApi.getBanners()).thenReturn(FakeBannerDataStore.bannerSuccessFlow)
        whenever(nemoApi.getCategories()).thenReturn(flowOf())

        val homeActivity = ActivityScenario.launch(HomeActivity::class.java)
        idlingRule.dataBindingIdlingResource.monitorActivity(homeActivity)

        assertDisplayed(R.id.bvp_home_banner)
    }

    @Test
    fun givenBanners_whenEmptyBanners_thenBannerNotDisplayed() {

        // Fake nemo api
        whenever(nemoApi.getBanners()).thenReturn(FakeBannerDataStore.bannerEmptySuccessFlow)
        whenever(nemoApi.getCategories()).thenReturn(flowOf())

        val homeActivity = ActivityScenario.launch(HomeActivity::class.java)
        idlingRule.dataBindingIdlingResource.monitorActivity(homeActivity)

        assertNotDisplayed(R.id.bvp_home_banner)
    }

    @Test
    fun givenBanners_whenClickedOnCategoryBanner_thenCategoriesLaunched() {
        // Fake nemo api
        whenever(nemoApi.getBanners()).thenReturn(FakeBannerDataStore.bannerSuccessFlow)
        whenever(nemoApi.getCategories()).thenReturn(FakeCategoryDataStore.categoriesSuccessFlow)
        whenever(nemoApi.getProducts(any(), any(), any())).thenReturn(flowOf())

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val homeActivity =
            ActivityScenario.launch<HomeActivity>(HomeActivity.getStartIntent(context))
        idlingRule.dataBindingIdlingResource.monitorActivity(homeActivity)

        Intents.init()
        clickOn(R.id.bvp_home_banner)
        intended(hasComponent(ProductsActivity::class.java.name))
        intended(hasExtraWithKey(ProductsActivity.KEY_CATEGORY))
        Intents.release()
    }

    @Test
    fun givenBanners_whenClickedOnProductBanner_thenProductDetailLaunched() {
        // Fake nemo api
        whenever(nemoApi.getBanners()).thenReturn(FakeBannerDataStore.bannerSuccessFlow)
        whenever(nemoApi.getCategories()).thenReturn(FakeCategoryDataStore.categoriesSuccessFlow)
        whenever(nemoApi.getProducts(any(), any(), any())).thenReturn(flowOf())
        whenever(nemoApi.getProduct(any())).thenReturn(flowOf())

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val homeActivity =
            ActivityScenario.launch<HomeActivity>(HomeActivity.getStartIntent(context))
        idlingRule.dataBindingIdlingResource.monitorActivity(homeActivity)

        Intents.init()
        swipeViewPagerForward()
        BaristaSleepInteractions.sleep(1000)
        clickOn(R.id.bvp_home_banner)
        intended(hasComponent(ProductDetailActivity::class.java.name))
        intended(hasExtraWithKey(ProductDetailActivity.KEY_PRODUCT_ID))
        Intents.release()
    }

    @Test
    fun givenCategories_whenGoodCategories_thenCategoriesDisplayed() {

        // Fake nemo api
        whenever(nemoApi.getBanners()).thenReturn(FakeBannerDataStore.bannerSuccessFlow)
        whenever(nemoApi.getCategories()).thenReturn(FakeCategoryDataStore.categoriesSuccessFlow)

        val homeActivity = ActivityScenario.launch(HomeActivity::class.java)
        idlingRule.dataBindingIdlingResource.monitorActivity(homeActivity)

        assertDisplayed(R.id.rv_categories)
        assertDisplayed(R.id.tv_label_categories)
    }

    @Test
    fun givenCategories_whenBadCategories_thenBothCategoriesAndBannerNotDisplayed() {

        // Fake nemo api
        whenever(nemoApi.getBanners()).thenReturn(FakeBannerDataStore.bannerSuccessFlow)
        whenever(nemoApi.getCategories()).thenReturn(FakeCategoryDataStore.categoriesErrorFlow)

        val homeActivity = ActivityScenario.launch(HomeActivity::class.java)
        idlingRule.dataBindingIdlingResource.monitorActivity(homeActivity)

        assertNotDisplayed(R.id.rv_categories)
        assertNotDisplayed(R.id.tv_label_categories)
        assertNotDisplayed(R.id.bvp_home_banner)
    }

    @Test
    fun givenBottomMenu_whenCartClicked_thenCartActivityLaunched() {

        // Fake nemo api
        whenever(nemoApi.getBanners()).thenReturn(FakeBannerDataStore.bannerSuccessFlow)
        whenever(nemoApi.getCategories()).thenReturn(FakeCategoryDataStore.categoriesSuccessFlow)

        ActivityScenario.launch(HomeActivity::class.java).run {
            idlingRule.dataBindingIdlingResource.monitorActivity(this)
            assertDisplayed(R.id.bnv_home_bottom_menu)

            Intents.init()
            clickOn(R.id.mi_home_cart)
            intended(hasComponent(CartActivity::class.java.name))
            Intents.release()
        }
    }
}