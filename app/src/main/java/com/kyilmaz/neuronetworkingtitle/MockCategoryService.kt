package com.kyilmaz.neuronetworkingtitle

import com.kyilmaz.neuronetworkingtitle.ui.theme.Mint
import com.kyilmaz.neuronetworkingtitle.ui.theme.Periwinkle
import com.kyilmaz.neuronetworkingtitle.ui.theme.SoftOrange
import com.kyilmaz.neuronetworkingtitle.ui.theme.SoftRed

object MockCategoryService {
    fun getCategories(): List<Category> {
        return listOf(
            Category("Neural Networks", SoftRed),
            Category("Cognitive Science", SoftOrange),
            Category("Machine Learning", Mint),
            Category("Neuroscience", Periwinkle)
        )
    }
}
