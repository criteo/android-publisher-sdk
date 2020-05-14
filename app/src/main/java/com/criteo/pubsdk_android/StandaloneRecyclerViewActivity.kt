package com.criteo.pubsdk_android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
  }

  class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataset: MutableList<Content> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      val inflater = LayoutInflater.from(parent.context)
      val view = inflater.inflate(R.layout.content_dummy, parent, false)

      return object: RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      val data = dataset[position]

      val textView = holder.itemView.findViewById<TextView>(R.id.text)
      val imageView = holder.itemView.findViewById<ImageView>(R.id.image)
      textView.text = data.text
      Picasso.get().load(data.imageUrl).fit().into(imageView)
    }

    override fun getItemCount() = dataset.size

    fun addContent() {
      dataset.add(Content.newRandom())
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