//
//  NewsListModel.swift
//  iosApp
//
//  Created by Anna Zharkova on 03.10.2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import shared

class NewsListModel : ObservableObject, INewsListView {
    var interactor: CoreIInteractor? = nil

    init() {
        self.interactor =  ConfigFactory.companion.instance.create(view: self)
        self.interactor?.attachView()
    }
    
    @Published var items: [NewsItem] = [NewsItem]()
    
    func setup() {
       
    }
    
    func setupNews(data: NewsList) {
        self.items = data.articles
    }
   
    func loadNews() {
        (interactor as? INewsListInteractor)?.loadNews()
    }
    
    func processNews(data: [NewsItem]) {
    }
}

