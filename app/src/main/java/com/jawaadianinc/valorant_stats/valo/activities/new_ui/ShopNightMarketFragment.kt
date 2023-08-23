package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jawaadianinc.valorant_stats.R

class ShopNightMarketViewModel : ViewModel() {
    private val _adapterData = MutableLiveData<WeaponNightSkinOfferAdapter?>()
    val adapterData: LiveData<WeaponNightSkinOfferAdapter?> = _adapterData

    fun setAdapterData(adapter: WeaponNightSkinOfferAdapter) {
        _adapterData.value = adapter
    }
}

class ShopNightMarketFragment : Fragment() {
    private lateinit var viewModel: ShopNightMarketViewModel
    private var adapter: WeaponNightSkinOfferAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop_offers, container, false)
    }

    companion object {
        fun newInstance(shopAdapter: WeaponNightSkinOfferAdapter): ShopNightMarketFragment {
            val fragment = ShopNightMarketFragment()
            fragment.adapter = shopAdapter
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ShopNightMarketViewModel::class.java]

        val listView: ListView = view.findViewById(R.id.playerStoreListView)
        val adapterDataObserver = Observer<WeaponNightSkinOfferAdapter?> { adapter ->
            if (adapter != null) {
                listView.adapter = adapter
            }
        }

        viewModel.adapterData.observe(viewLifecycleOwner, adapterDataObserver)
        adapter?.let { viewModel.setAdapterData(it) }

    }
}
