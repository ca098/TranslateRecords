package com.example.myapplication.RecyclerView

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.queryResponse
import com.example.myapplication.R
import kotlinx.android.synthetic.main.recyclerlayout.view.*


/**
 * RecyclerView for the list of records for a user.
 * Takes in an Array list and sets the text for the corresponding values
 */
class RecyclerAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var items: List<queryResponse> = ArrayList()
    var initText = ""
    var langText = ""
    var transText = ""
    var dateCreateText = ""
    var idText = ""


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BlogViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recyclerlayout, parent, false)
        )
    }

    /**
     * Send the position and items to bind method
     * Set on click listener for each query. The query that is selected, all of the text from that block
     * will be extracted and sent to details activity
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {

            is BlogViewHolder -> {
                holder.bind(items.get(position))


                holder.itemView.setOnClickListener {

                    initText = items.get(position).initialText
                    langText = items.get(position).language
                    transText = items.get(position).translatedText
                    dateCreateText = items.get(position).date_created
                    idText = items.get(position).id

                    val context=holder.initial_text.context
                    val intent = Intent( context, queryDetails::class.java)
                    intent.putExtra("initialText", initText)
                    intent.putExtra("langText", langText)
                    intent.putExtra("transText", transText)
                    intent.putExtra("dateCreate", dateCreateText)
                    intent.putExtra("idText", idText)
                    context.startActivity(intent)


                }

            }

        }
    }


    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(blogList: List<queryResponse>){
        items = blogList
        notifyDataSetChanged()
    }

    fun update(){
        notifyDataSetChanged()
    }



    class BlogViewHolder
    constructor(itemView: View): RecyclerView.ViewHolder(itemView){





        val initial_text = itemView.initialTextRecy
        val language = itemView.languageRecy
        val translated_text = itemView.translatedTextRecy
        val date_created = itemView.dateCreatedRecy


        /**
         * Set the text of the textviews to the data
         */
        fun bind(blogPost: queryResponse){


            initial_text.setText(blogPost.initialText)
            language.setText(blogPost.language)
            translated_text.setText(blogPost.translatedText)
            date_created.setText(blogPost.date_created)



        }



    }


    }
