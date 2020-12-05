/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 skydoves
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
@file:Suppress("unused")

package com.skydoves.elasticviews

import android.view.View
import android.view.ViewGroup
import android.view.animation.CycleInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener

/** for create ElasticAnimation by kotlin dsl. */
@JvmSynthetic
fun elasticAnimation(view: View, block: ElasticAnimation.() -> Unit): ElasticAnimation =
  ElasticAnimation(view).apply(block)

/** ElasticAnimation implements elastic animations for android views or view groups. */
class ElasticAnimation(private val view: View) {

  @JvmField
  var scaleX = 0.7f

  @JvmField
  var scaleY = 0.7f

  @JvmField
  var duration = 400

  @JvmField
  var listener: ViewPropertyAnimatorListener? = null

  @JvmField
  var finishListener: ElasticFinishListener? = null
  private var isAnimating: Boolean = false

  fun setScaleX(scaleX: Float): ElasticAnimation = apply { this.scaleX = scaleX }
  fun setScaleY(scaleY: Float): ElasticAnimation = apply { this.scaleY = scaleY }
  fun setDuration(duration: Int): ElasticAnimation = apply { this.duration = duration }
  fun setListener(listener: ViewPropertyAnimatorListener): ElasticAnimation = apply {
    this.listener = listener
  }

  fun setOnFinishListener(finishListener: ElasticFinishListener?): ElasticAnimation = apply {
    this.finishListener = finishListener
  }

  @JvmSynthetic
  fun setOnFinishListener(block: () -> Unit): ElasticAnimation = apply {
    this.finishListener = ElasticFinishListener { block() }
  }

  /** starts elastic animation. */
  fun doAction() {
    if (!this.isAnimating && this.view.scaleX == 1f) {
      val animatorCompat = ViewCompat.animate(view)
        .setDuration(this.duration.toLong())
        .scaleX(this.scaleX)
        .scaleY(this.scaleY)
        .setInterpolator(CycleInterpolator(0.5f)).apply {
          listener?.let { setListener(it) } ?: setListener(object : ViewPropertyAnimatorListener {
            override fun onAnimationCancel(view: View?) = Unit
            override fun onAnimationStart(view: View?) {
              isAnimating = true
            }

            override fun onAnimationEnd(view: View?) {
              finishListener?.onFinished()
              isAnimating = false
            }
          })
        }
      if (this.view is ViewGroup) {
        for (index in 0 until this.view.childCount) {
          val nextChild = this.view.getChildAt(index)
          ViewCompat.animate(nextChild)
            .setDuration(this.duration.toLong())
            .scaleX(this.scaleX)
            .scaleY(this.scaleY)
            .setInterpolator(CycleInterpolator(0.5f))
            .withLayer()
            .start()
        }
      }
      animatorCompat.withLayer().start()
    }
  }

  fun isAnimating(): Boolean = this.isAnimating
}
