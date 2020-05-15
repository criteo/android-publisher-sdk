package com.criteo.pubsdk_android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.criteo.publisher.CriteoBannerView
import com.criteo.pubsdk_android.PubSdkDemoApplication.STANDALONE_BANNER
import com.criteo.pubsdk_android.listener.TestAppBannerAdListener
import com.squareup.picasso.Picasso
import kotlin.random.Random

class StandaloneRecyclerViewActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_standalone_recycler_view)

    val viewAdapter = Adapter()
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
  }

  private fun loadBanner(adapter: Adapter) {
    val bannerView = CriteoBannerView(baseContext, STANDALONE_BANNER)
    bannerView.setCriteoBannerAdListener(object : TestAppBannerAdListener(javaClass.simpleName, "Banner", null) {
      override fun onAdReceived(view: View?) {
        adapter.addBannerAd(bannerView)
      }
    })

    bannerView.loadAd()
  }

  class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataset: MutableList<Any> = mutableListOf()

    private companion object {
      const val CONTENT = 1
      const val BANNER = 2
    }

    override fun getItemViewType(position: Int): Int {
      return when (dataset[position]) {
        is Content -> CONTENT
        is CriteoBannerView -> BANNER
        else -> throw NotImplementedError()
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      val context = parent.context
      val view = when (viewType) {
        CONTENT -> {
          val inflater = LayoutInflater.from(context)
          inflater.inflate(R.layout.content_dummy, parent, false)
        }
        BANNER -> FrameLayout(context)
        else -> throw NotImplementedError()
      }

      return object: RecyclerView.ViewHolder(view) {}
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

  }

  data class Content(val text: String, val imageUrl: String) {
    companion object {
      private val random = Random.Default

      fun newRandom(): Content {
        val width = random.nextInt(300, 500)
        val height = random.nextInt(100, 300)
        val imageUrl = "https://placekitten.com/$width/$height"
        return Content(imageUrl, imageUrl)
      }
    }
  }

}