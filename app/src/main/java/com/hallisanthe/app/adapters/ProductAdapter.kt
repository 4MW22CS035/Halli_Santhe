package com.hallisanthe.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hallisanthe.app.R
import com.hallisanthe.app.data.model.Product
import com.hallisanthe.app.databinding.ItemProductBinding

class ProductAdapter(
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val products = mutableListOf<Product>()

    fun submitList(newItems: List<Product>) {
        products.clear()
        products.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = products.getOrNull(position) ?: return
        holder.bind(item)
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) = with(binding) {
            Glide.with(imageProduct)
                .load(product.imageUrl)
                .placeholder(R.drawable.bg_card_placeholder)
                .error(R.drawable.bg_card_placeholder)
                .centerCrop()
                .into(imageProduct)
            textName.text = product.name
            textCategory.text = product.category
            textPrice.text = itemView.context.getString(R.string.price_format, product.price)
            textStock.text = if (product.inStock) {
                itemView.context.getString(R.string.in_stock)
            } else {
                itemView.context.getString(R.string.out_of_stock)
            }
            textStock.setTextColor(
                itemView.context.getColor(
                    if (product.inStock) R.color.market_green else R.color.market_maroon
                )
            )
            chipOutOfStock.visibility = if (product.inStock) View.GONE else View.VISIBLE
            root.setOnClickListener { onProductClick(product) }
        }
    }
}
