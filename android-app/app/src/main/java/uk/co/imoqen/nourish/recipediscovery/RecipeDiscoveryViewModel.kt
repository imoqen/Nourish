package uk.co.imoqen.nourish.recipediscovery

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import uk.co.imoqen.nourish.network.ApiService
import javax.inject.Inject

@HiltViewModel
class RecipeDiscoveryViewModel @Inject constructor(
    private val ApiService: ApiService
): ViewModel() {
}