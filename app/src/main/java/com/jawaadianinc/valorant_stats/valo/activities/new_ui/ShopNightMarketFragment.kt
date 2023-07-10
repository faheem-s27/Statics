package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R



class ShopNightMarketFragment : Fragment() {
    private var adapter: WeaponSkinOfferAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop_offers, container, false)
    }

    fun newInstance(shopAdapter: WeaponSkinOfferAdapter): ShopNightMarketFragment {
        val fragment = ShopNightMarketFragment()
        adapter = shopAdapter
        return fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView : ListView = view.findViewById(R.id.playerStoreListView)
        if (adapter != null) {
            listView.adapter = adapter
        }

    }
}