package com.example.jot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

public class ItemTouchUtil {
    interface Actions{
        void move(int fromPos, int toPos);
        String delete(int pos);
        void undoDelete();
    }

    private static ColorDrawable colorBackground;
    private static Drawable deleteIcon;

    public static ItemTouchHelper.SimpleCallback make(Context ctx, final Actions actions){
        colorBackground = new ColorDrawable(ctx.getColor(R.color.colorDelete));
        deleteIcon = ctx.getDrawable(android.R.drawable.ic_menu_delete);

        return new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recycler,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                //get to and from positions
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();

                //move the item
                actions.move(fromPos, toPos);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int pos = viewHolder.getAdapterPosition();

                //remove the item
                String obj_name = actions.delete(pos);

                Snackbar.make(viewHolder.itemView, "Deleted " + obj_name,
                        Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        actions.undoDelete();
                    }
                }).show();
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView rView,
                                    RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState,
                                    boolean isActive){
                if(dX > 0){
                    //draw a red background behind the element with a delete icon
                    View iv = viewHolder.itemView;

                    int vert_margin = (viewHolder.itemView.getHeight() -
                            deleteIcon.getIntrinsicHeight()) / 2;


                    colorBackground.setBounds(iv.getLeft(), iv.getTop(), (int) dX, iv.getBottom());
                    deleteIcon.setBounds(
                            iv.getLeft() + vert_margin,
                            iv.getTop() + vert_margin,
                            iv.getLeft() + vert_margin + deleteIcon.getIntrinsicWidth(),
                            iv.getBottom() - vert_margin);

                    colorBackground.draw(c);
                    c.save();

                    c.clipRect(iv.getLeft(), iv.getTop(), (int) dX, iv.getBottom());

                    deleteIcon.draw(c);
                    c.restore();
                }

                super.onChildDraw(c, rView, viewHolder, dX, dY, actionState, isActive);
            }
        };
    }
}
