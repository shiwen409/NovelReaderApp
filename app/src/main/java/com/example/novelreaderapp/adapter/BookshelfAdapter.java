package com.example.novelreaderapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.novelreaderapp.R;
import com.example.novelreaderapp.entity.Book;
import com.squareup.picasso.Picasso;
import java.util.List;

public class BookshelfAdapter extends BaseAdapter {
    private Context context;
    private List<Book> bookList;
    private OnDeleteListener deleteListener;

    public void updateData(List<Book> newBookList) {
        this.bookList = newBookList;
    }

    // 构造方法
    public BookshelfAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    // 删除按钮点击接口
    public interface OnDeleteListener {
        void onDelete(Book book);
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    // 列表长度
    @Override
    public int getCount() {
        return bookList.size();
    }

    // 获取当前项数据
    @Override
    public Object getItem(int position) {
        return bookList.get(position);
    }

    // 获取当前项ID
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 绑定列表项视图
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        // 复用视图（优化性能）
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
            holder = new ViewHolder();
            holder.ivCover = convertView.findViewById(R.id.iv_book_cover);
            holder.tvTitle = convertView.findViewById(R.id.tv_book_title);
            holder.tvAuthor = convertView.findViewById(R.id.tv_book_author);
            holder.btnDelete = convertView.findViewById(R.id.btn_add_to_bookshelf);
            // 修改按钮文字为“删除”
            holder.btnDelete.setText("删除");
            holder.btnDelete.setBackgroundColor(context.getResources().getColor(R.color.colorRed));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 绑定数据
        Book book = bookList.get(position);
        holder.tvTitle.setText(book.getName());
        holder.tvAuthor.setText("作者：" + book.getAuthor());
        if (book.getCover() != null && !book.getCover().isEmpty()) {
            Picasso.get().load(book.getCover()).into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_book);
        }

        // 删除按钮点击事件
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteListener != null) {
                    deleteListener.onDelete(book);
                }
            }
        });

        return convertView;
    }

    // ViewHolder类
    static class ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        TextView tvAuthor;
        Button btnDelete;
    }
}