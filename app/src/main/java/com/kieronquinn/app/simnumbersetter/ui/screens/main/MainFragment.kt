package com.kieronquinn.app.simnumbersetter.ui.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.simnumbersetter.R
import com.kieronquinn.app.simnumbersetter.databinding.FragmentMainBinding
import com.kieronquinn.app.simnumbersetter.ui.screens.main.MainViewModel.*
import com.kieronquinn.app.simnumbersetter.utils.extensions.hideIme
import com.kieronquinn.app.simnumbersetter.utils.extensions.onChanged
import com.kieronquinn.app.simnumbersetter.utils.extensions.onClicked
import com.kieronquinn.monetcompat.app.MonetFragment
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.take
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.NullPointerException

class MainFragment: Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding
        ?: throw NullPointerException("Unable to access binding before onViewCreated or after onDestroyView")

    private val viewModel by viewModel<MainViewModel>()
    private val monet by lazy {
        MonetCompat.getInstance()
    }

    private val bottomPadding by lazy {
        resources.getDimension(R.dimen.margin_16).toInt()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInsets()
        setupState()
        setupToolbar()
        setupNumber()
        setupMonet()
        setupSaveButton()
        setupCloseButton()
        setupInput()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainToolbar) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = topInset)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.includeMainLoaded.root) { view, insets ->
            val bottomInset = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            ).bottom
            view.updatePadding(bottom = bottomPadding + bottomInset)
            insets
        }
    }

    private fun setupMonet() = with(binding) {
        includeMainLoading.mainLoadingProgress.applyMonet()
        includeMainError.mainErrorClose.applyMonet()
        val background = monet.getBackgroundColorSecondary(requireContext())
            ?: monet.getPrimaryColor(requireContext())
        with(includeMainLoaded.includeMainLoadedCardInfo){
            root.setCardBackgroundColor(background)
        }
        with(includeMainLoaded.includeMainLoadedCardEdit) {
            root.setCardBackgroundColor(background)
            mainLoadedCardEditInput.applyMonet()
            mainLoadedCardEditEdit.applyMonet()
            mainLoadedCardEditInputSave.applyMonet()
        }
    }

    private fun setupToolbar() = with(binding.mainToolbar) {
        MenuInflater(requireContext()).inflate(R.menu.menu, menu)
        setOnMenuItemClickListener {
            viewModel.onMenuItemClicked(requireContext(), it.itemId)
            true
        }
    }

    private fun setupSaveButton() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        with(binding.includeMainLoaded.includeMainLoadedCardEdit.mainLoadedCardEditInputSave) {
            onClicked().collect {
                it.hideIme()
                viewModel.onSaveClicked()
            }
        }
    }

    private fun setupCloseButton() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        with(binding.includeMainError.mainErrorClose) {
            onClicked().collect {
                requireActivity().finish()
            }
        }
    }

    private fun setupInput() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        with(binding.includeMainLoaded.includeMainLoadedCardEdit.mainLoadedCardEditEdit){
            onChanged().collect {
                viewModel.onNumberChanged(it?.toString() ?: "")
            }
        }
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) {
        binding.includeMainLoading.root.isVisible = state is State.Loading
        binding.includeMainLoaded.root.isVisible = state is State.Loaded
        binding.includeMainError.root.isVisible = state is State.Error
        when(state) {
            is State.Loading -> setupWithLoading(state.loadType)
            is State.Error -> setupWithError(state.errorType)
            is State.Loaded -> setupWithLoaded(state.number)
        }
    }

    private fun setupNumber() {
        viewModel.number.value?.let {
            handleNumber(it)
        } ?: run {
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                viewModel.number.filterNotNull().take(1).collect {
                    handleNumber(it)
                }
            }
        }
    }

    private fun handleNumber(number: String) = with(binding.includeMainLoaded.includeMainLoadedCardEdit) {
        mainLoadedCardEditEdit.setText(number, TextView.BufferType.EDITABLE)
    }

    private fun setupWithLoading(loadType: LoadType) = with(binding.includeMainLoading) {
        mainLoading.setText(loadType.contentRes)
    }

    private fun setupWithError(errorType: ErrorType) = with(binding.includeMainError) {
        mainErrorContent.setText(errorType.messageRes)
    }

    private fun setupWithLoaded(number: String) = with(binding.includeMainLoaded) {
        val formattedNumber = number.ifEmpty {
            getString(R.string.main_loaded_card_edit_number_empty)
        }
        includeMainLoadedCardEdit.mainLoadedCardEditCurrent.text =
            getString(R.string.main_loaded_card_edit_content, formattedNumber)
    }

}