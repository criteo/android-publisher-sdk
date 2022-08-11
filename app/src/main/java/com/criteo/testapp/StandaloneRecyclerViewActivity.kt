/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.testapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.criteo.publisher.CriteoBannerView
import com.criteo.publisher.advancednative.CriteoNativeAd
import com.criteo.publisher.advancednative.CriteoNativeLoader
import com.criteo.testapp.PubSdkDemoApplication.Companion.BANNER
import com.criteo.testapp.PubSdkDemoApplication.Companion.CONTEXT_DATA
import com.criteo.testapp.PubSdkDemoApplication.Companion.NATIVE
import com.criteo.testapp.listener.TestAppBannerAdListener
import com.criteo.testapp.listener.TestAppNativeAdListener
import com.squareup.picasso.Picasso
import kotlin.random.Random

class StandaloneRecyclerViewActivity : BaseActivity() {

  private lateinit var nativeLoader: CriteoNativeLoader
  private lateinit var viewAdapter: Adapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_standalone_recycler_view)

    nativeLoader = CriteoNativeLoader(
        NATIVE,
        object : TestAppNativeAdListener(javaClass.simpleName, "Native", null) {
          override fun onAdReceived(nativeAd: CriteoNativeAd) {
            viewAdapter.addNativeAd(nativeAd)
          }
        },
        TestAppNativeRenderer()
    )

    viewAdapter = Adapter(nativeLoader)
    val viewManager = LinearLayoutManager(this)
    findViewById<RecyclerView>(R.id.recyclerView).apply {
      layoutManager = viewManager
      adapter = viewAdapter
    }

    findViewById<View>(R.id.buttonStandaloneContent).setOnClickListener {
      viewAdapter.addContent()
    }

    findViewById<View>(R.id.buttonStandaloneBanner).setOnClickListener {
      loadBanner(viewAdapter)
    }

    findViewById<View>(R.id.buttonStandaloneNative).setOnClickListener {
      nativeLoader.loadAd(CONTEXT_DATA)
    }
  }

  private fun loadBanner(adapter: Adapter) {
    val bannerView = CriteoBannerView(baseContext, BANNER)
    bannerView.setCriteoBannerAdListener(object : TestAppBannerAdListener(javaClass.simpleName, "Banner") {
      override fun onAdReceived(view: CriteoBannerView) {
        adapter.addBannerAd(view)
      }
    })

    bannerView.loadAd(CONTEXT_DATA)
  }

  class Adapter(private val nativeLoader: CriteoNativeLoader) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataset: MutableList<Any> = mutableListOf()

    private companion object {
      const val CONTENT = 1
      const val BANNER = 2
      const val NATIVE = 3
    }

    @Suppress("NotImplementedDeclaration")
    override fun getItemViewType(position: Int): Int {
      return when (dataset[position]) {
        is Content -> CONTENT
        is CriteoBannerView -> BANNER
        is CriteoNativeAd -> NATIVE
        else -> throw NotImplementedError()
      }
    }

    @Suppress("NotImplementedDeclaration")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      val context = parent.context
      val view = when (viewType) {
        CONTENT -> {
          val inflater = LayoutInflater.from(context)
          inflater.inflate(R.layout.content_dummy, parent, false)
        }
        BANNER -> FrameLayout(context)
        NATIVE -> nativeLoader.createEmptyNativeView(context, parent)
        else -> throw NotImplementedError()
      }

      return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      val viewType = getItemViewType(position)
      val rawData = dataset[position]
      val view = holder.itemView

      when (viewType) {
        CONTENT -> {
          val content = rawData as Content
          val textView = view.findViewById<TextView>(R.id.text)
          val imageView = view.findViewById<ImageView>(R.id.image)
          textView.text = content.text
          Picasso.get().load(content.imageUrl).fit().into(imageView)
        }
        BANNER -> {
          val banner = rawData as CriteoBannerView
          val layout = view as ViewGroup
          (banner.parent as? ViewGroup)?.removeView(banner)
          layout.addView(banner)
        }
        NATIVE -> {
          val nativeAd = rawData as CriteoNativeAd
          nativeAd.renderNativeView(view)
        }
      }
    }

    override fun getItemCount() = dataset.size

    fun addContent() {
      dataset.add(Content.newRandom())
      notifyItemInserted(dataset.size - 1)
    }

    fun addBannerAd(bannerView: CriteoBannerView) {
      dataset.add(bannerView)
      notifyItemInserted(dataset.size - 1)
    }

    fun addNativeAd(nativeAd: CriteoNativeAd) {
      dataset.add(nativeAd)
      notifyItemInserted(dataset.size - 1)
    }
  }

  data class Content(val text: String, val imageUrl: String) {
    companion object {
      private val random = Random.Default

      @Suppress("MagicNumber")
      fun newRandom(): Content {
        val width = random.nextInt(300, 500)
        val height = random.nextInt(100, 300)
        val imageUrl = "https://placekitten.com/$width/$height"
        return Content(imageUrl, imageUrl)
      }
    }
  }
}
