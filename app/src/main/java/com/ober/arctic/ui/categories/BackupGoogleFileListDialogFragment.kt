package com.ober.arctic.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ober.arctic.ui.BaseDialogFragment
import com.ober.arcticpass.R

class BackupGoogleFileListDialogFragment: BaseDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return setAndBindContentView(inflater, container!!, R.layout.fragment_file_list)
    }
}