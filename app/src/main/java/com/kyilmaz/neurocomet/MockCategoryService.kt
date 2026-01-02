package com.kyilmaz.neurocomet

import com.kyilmaz.neurocomet.ui.theme.Mint
import com.kyilmaz.neurocomet.ui.theme.Periwinkle
import com.kyilmaz.neurocomet.ui.theme.SoftOrange
import com.kyilmaz.neurocomet.ui.theme.SoftRed

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
