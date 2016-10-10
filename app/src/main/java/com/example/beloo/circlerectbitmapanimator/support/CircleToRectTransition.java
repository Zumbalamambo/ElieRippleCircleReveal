package com.example.beloo.circlerectbitmapanimator.support;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.beloo.circlerectbitmapanimator.view.CircleRectView;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class CircleToRectTransition extends Transition {
    private static final String BOUNDS = "viewBounds";
    private static final String[] PROPS = {BOUNDS};

    @Override
    public String[] getTransitionProperties() {
        return PROPS;
    }

    private void captureValues(TransitionValues transitionValues) {
        View view = transitionValues.view;
        Rect bounds = new Rect();
        bounds.left = view.getLeft();
        bounds.right = view.getRight();
        bounds.top = view.getTop();
        bounds.bottom = view.getBottom();
        transitionValues.values.put(BOUNDS, bounds);
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }

        if (!(startValues.view instanceof CircleRectView)) {
            Log.w(CircleToRectTransition.class.getSimpleName(), "transition view should be CircleRectView");
            return null;
        }

        CircleRectView view = (CircleRectView) (startValues.view);

        Rect startRect = (Rect) startValues.values.get(BOUNDS);
        final Rect endRect = (Rect) endValues.values.get(BOUNDS);

        Animator animator;

        //scale animator
        animator = view.animator(startRect.height(), startRect.width(), endRect.height(), endRect.width());

        //movement animators below
        //if some translation not performed fully, use it instead of start coordinate
        int moveX = Math.round(view.getTranslationX()) == 0 ? startRect.left : Math.round(view.getTranslationX());
        int moveY = Math.round(view.getTranslationY()) == 0 ? startRect.top : Math.round(view.getTranslationY());

        //somehow end rect returns needed value minus translation in case not finished transition available
        int moveXTo = Math.round(view.getTranslationX()) == 0 ? endRect.left : endRect.left + Math.round(view.getTranslationX());
        int moveYTo = Math.round(view.getTranslationY()) == 0 ? endRect.top : endRect.top + Math.round(view.getTranslationY());

        Animator moveXAnimator = ObjectAnimator.ofFloat(view, "translationX", moveX, moveXTo);
        Animator moveYAnimator = ObjectAnimator.ofFloat(view, "translationY", moveY, moveYTo);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveXAnimator, moveYAnimator, animator);

        //prevent blinking when interrupt animation
        return new NoPauseAnimator(animatorSet);
    }
}
