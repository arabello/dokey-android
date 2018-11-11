package io.rocketguys.dokey.intro

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.rocketguys.dokey.R
import kotlinx.android.synthetic.main.activity_connect.view.*
import kotlinx.android.synthetic.main.fragment_intro.view.*

private const val ARG_PAGE_INDEX = "arg_page_index"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [IntroFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [IntroFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class IntroFragment : Fragment() {
    private var argPageIndex: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            argPageIndex = it.getInt(ARG_PAGE_INDEX)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var frag = inflater.inflate(R.layout.fragment_intro, container, false)

        when(argPageIndex){
            0 -> {
                frag.introFragTitle.text = getString(R.string.frag_intro_title_0)
                frag.introFragDesc.text = getString(R.string.frag_intro_desc_0)
                frag.introFragImg.setImageResource(R.drawable.intro_img_0)
            }
            1 -> {
                frag.introFragTitle.text = getString(R.string.frag_intro_title_1)
                frag.introFragDesc.text = getString(R.string.frag_intro_desc_1)
                frag.introFragImg.setImageResource(R.drawable.intro_img_1)
            }
            2 -> {
                frag.introFragTitle.text = getString(R.string.frag_intro_title_2)
                frag.introFragDesc.text = getString(R.string.frag_intro_desc_2)
                frag.introFragImg.setImageResource(R.drawable.intro_img_2)
            }
            3 -> {
                frag.introFragTitle.text = getString(R.string.frag_intro_title_3)
                frag.introFragDesc.text = getString(R.string.frag_intro_desc_3)
                frag.introFragImg.setImageResource(R.drawable.intro_img_3)
            }
            4 -> {
                frag = inflater.inflate(R.layout.fragment_intro_finish, container, false)
                frag.introFragTitle.text = getString(R.string.frag_intro_title_4)
                frag.introFragDesc.text = getString(R.string.frag_intro_desc_4)
                frag.introFragImg.setImageResource(R.drawable.intro_img_4)
                frag.introFragScanBtn.setOnClickListener {
                    (activity as IntroActivity).onIntroCompleted()
                }
            }
        }

        return frag
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
                IntroFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_PAGE_INDEX, param1)
                    }
                }
    }
}
