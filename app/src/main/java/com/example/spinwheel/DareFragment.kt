package com.example.spinwheel

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment

class DareFragment : Fragment() {
    private val cards = mutableListOf<DareCard>()
    private var selectedIndex = -1
    private var soundOn = true
    private val selectedNumber: Int
        get() = arguments?.getInt(ARG_SELECTED_NUMBER, DEFAULT_SELECTED_NUMBER) ?: DEFAULT_SELECTED_NUMBER

    private val challenges = listOf(
        "Make a lovey-dovey face for 5 seconds",
        "Raise your hand and shout: I am the chosen one",
        "Do a belly dance for 5 minutes straight",
        "Call your mom and say you were arrested",
        "Drink some Tequila",
        "Eat some raw eggs",
        "Sing a chorus loudly",
        "Tell one funny secret",
        "Do ten squats",
        "Speak with an accent for one minute",
        "Let the group choose a nickname",
        "Say something nice to every player",
        "Pose like a superstar",
        "Read the last message you sent",
        "Let someone pick your next challenge",
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_dare, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTopBar(view)
        setupDareGrid(view.findViewById(R.id.dare_grid))
    }

    private fun setupTopBar(view: View) {
        view.findViewById<TextView>(R.id.tv_number).text = selectedNumber.toString()
        view.findViewById<View>(R.id.iv_left).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        view.findViewById<ImageView>(R.id.iv_right).setOnClickListener { volumeView ->
            soundOn = !soundOn
            (volumeView as ImageView).setImageResource(
                if (soundOn) R.drawable.ic_volume_up_black else R.drawable.ic_volume_off_black
            )
        }
    }

    private fun setupDareGrid(grid: GridLayout) {
        cards.clear()
        selectedIndex = -1
        val itemCount = minOf(grid.childCount, challenges.size)
        for (index in 0 until itemCount) {
            val cardView = grid.getChildAt(index) as? FrameLayout ?: continue
            val iconView = cardView.getChildAt(0) as? ImageView ?: continue
            val challengeView = createChallengeView(index)

            cardView.addView(challengeView)
            cardView.cameraDistance = resources.displayMetrics.density * 8000
            cardView.isClickable = true
            cardView.isFocusable = true
            cardView.setOnClickListener { selectCard(index) }

            cards += DareCard(cardView, iconView, challengeView)
            setCardSelected(index, selected = false)
        }
    }

    private fun createChallengeView(index: Int): TextView {
        return TextView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(6.dp, 6.dp, 6.dp, 6.dp)
            text = challenges[index]
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(ContextCompat.getColor(requireContext(), R.color.color_main_text))
            textSize = 12f
            typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_semibold)
            visibility = View.GONE
        }
    }

    private fun selectCard(index: Int) {
        if (index == selectedIndex || index !in cards.indices) return

        val previousIndex = selectedIndex
        selectedIndex = index
        if (previousIndex in cards.indices) {
            cards[previousIndex].container.animate().cancel()
            cards[previousIndex].container.rotationY = 0f
            setCardSelected(previousIndex, selected = false)
        }

        flipSelectedCard(index)
    }

    private fun flipSelectedCard(index: Int) {
        val card = cards[index].container
        card.animate().cancel()
        card.animate()
            .rotationY(90f)
            .setDuration(120L)
            .withEndAction {
                setCardSelected(index, selected = true)
                card.rotationY = -90f
                card.animate()
                    .rotationY(0f)
                    .setDuration(120L)
                    .start()
            }
            .start()
    }

    private fun setCardSelected(index: Int, selected: Boolean) {
        val card = cards[index]
        card.container.setBackgroundResource(
            if (selected) R.drawable.bg_dare_item_selected else R.drawable.bg_dare_item
        )
        card.icon.visibility = if (selected) View.GONE else View.VISIBLE
        card.challenge.visibility = if (selected) View.VISIBLE else View.GONE
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private data class DareCard(
        val container: FrameLayout,
        val icon: ImageView,
        val challenge: TextView,
    )

    companion object {
        private const val ARG_SELECTED_NUMBER = "selected_number"
        private const val DEFAULT_SELECTED_NUMBER = 2

        fun newInstance(selectedNumber: Int): DareFragment {
            return DareFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SELECTED_NUMBER, selectedNumber)
                }
            }
        }
    }
}
