package com.azharkova.kmmkspcases.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.azharkova.core.IInteractor
import com.azharkova.kmmkspcases.INewsListInteractor
import com.azharkova.kmmkspcases.INewsListView
import com.azharkova.kmmkspcases.android.R
import com.azharkova.kmmkspcases.android.adapter.NewsAdapter
import com.azharkova.kmmkspcases.data.NewsList
import com.azharkova.kmmkspcases.interactor
import com.azharkova.kmmkspcases.setup

class NewsActivity : AppCompatActivity(), INewsListView {

    override var interactor: IInteractor? = null

    private var listView: RecyclerView? = null


    private var adapter: NewsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        listView = findViewById<RecyclerView>(R.id.news_list)
        listView?.layoutManager = LinearLayoutManager(this)
        setup()
    }

    override fun setupNews(data: NewsList) {
        if (adapter == null){
            adapter = NewsAdapter()
        }
        listView?.adapter = adapter
        adapter?.update(data.articles)
    }

    override fun onResume() {
        super.onResume()
        interactor()?.loadNews()
    }

}
