package antitelegram.devenirchef

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import antitelegram.devenirchef.data.Recipe
import com.bumptech.glide.Glide

class RecipesAdapter(val context: Context) : RecyclerView.Adapter<RecipesAdapter.ViewHolder>() {
    private var dataset: List<Recipe>? = null

    fun changeDataset(list: List<Recipe>) {
        dataset = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindData(dataset?.get(position))

        val openRecipeActivity = View.OnClickListener {
            val intent = Intent(context, RecipeActivity::class.java)
            intent.putExtra("recipe", dataset?.get(position))
            startActivity(context, intent, null)
        }

        holder?.text?.setOnClickListener(openRecipeActivity)
        holder?.recipeImage?.setOnClickListener(openRecipeActivity)

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.recipe_main_screen, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataset?.size!!
    }


    inner class ViewHolder(var view: View?) : RecyclerView.ViewHolder(view) {

        val text by lazy { view?.findViewById<TextView>(R.id.recipe_name) }
        val starsContainer by lazy { view?.findViewById<LinearLayout>(R.id.recipe_star_container) }
        val recipeImage by lazy { view?.findViewById<ImageView>(R.id.recipe_image) }
        val description by lazy { view?.findViewById<TextView>(R.id.recipe_description) }
        val tags by lazy { view?.findViewById<LinearLayout>(R.id.tags_container) }

        fun bindData(newRecipe: Recipe?) {
            text?.setText(newRecipe?.getTitle())
            val level = newRecipe?.getLevel()

            for (i in 0 until level!!) {
                starsContainer?.getChildAt(i)?.visibility = View.VISIBLE
            }
            for (i in level until 5) {
                starsContainer?.getChildAt(i)?.visibility = View.GONE
            }


            setImageToView(recipeImage!!, newRecipe.getPhotoUrl())
            description?.setText(newRecipe.getDescription())


            tags?.removeAllViews()
            if (newRecipe.tags != null) {
                for (tag in newRecipe.tags) {
                    val tagView = TextView(view?.context)
                    tagView.text = tag
                    tags?.addView(tagView)
                }

            }


        }

        private fun setImageToView(image: ImageView, photoUrl: String) {
            if (context is Activity && !context.isFinishing) {
                Glide.with(image.context)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .crossFade()
                        .into(image)
            }
        }

    }
}