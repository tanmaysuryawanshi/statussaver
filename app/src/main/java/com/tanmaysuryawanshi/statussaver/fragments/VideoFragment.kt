package com.tanmaysuryawanshi.statussaver.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tanmaysuryawanshi.statussaver.R


class VideoFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v= inflater.inflate(R.layout.fragment_video, container, false)

   return v
    }


}