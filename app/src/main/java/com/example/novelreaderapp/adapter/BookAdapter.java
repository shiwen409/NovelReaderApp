package com.example.novelreaderapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.novelreaderapp.R;
import com.example.novelreaderapp.entity.Book;
import com.squareup.picasso.Picasso; // 需添加Picasso依赖（用于加载网络图片）

import java.util.ArrayList;
import java.util.List;

// 添加Picasso依赖（在build.gradle的dependencies中添加）：
// implementation 'com.squareup.picasso:picasso:2.71828'

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private Context context;
    //private List<Book> bookList;
    private OnAddBookshelfListener addListener; // 加入书架点击事件
    private OnBookClickListener bookClickListener; // 书籍项点击事件（跳转到详情）

    private List<Book> bookList = new ArrayList<>(); // 数据源
    // 构造方法
    public BookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    // 设置加入书架点击监听
    public void setOnAddBookshelfListener(OnAddBookshelfListener listener) {
        this.addListener = listener;
    }

    // 设置书籍项点击监听
    public void setOnBookClickListener(OnBookClickListener listener) {
        this.bookClickListener = listener;
    }

    public void setBooks(List<Book> newBooks) {
        this.bookList = newBooks; // 使用新数据替换旧数据
        notifyDataSetChanged(); // 通知列表刷新UI
    }

    // 点击事件接口
    public interface OnAddBookshelfListener {
        void onAdd(Book book);
    }

    public interface OnBookClickListener {
        void onClick(Book book);
    }

    // 创建ViewHolder（加载列表项布局）
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    // 绑定数据到ViewHolder
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        // 书籍名称
        holder.tvTitle.setText(book.getName());
        // 作者
        holder.tvAuthor.setText("作者：" + book.getAuthor());
        // 加载封面图片（使用Picasso，网络图片为空时显示默认图标）
        if (book.getCover() != null && !book.getCover().isEmpty()) {
            Picasso.get().load(book.getCover()).into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_book);
        }

        // 加入书架按钮点击事件
        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addListener != null) {
                    addListener.onAdd(book);
                }
            }
        });

        // 书籍项点击事件（跳转到详情）
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookClickListener != null) {
                    bookClickListener.onClick(book);
                }
            }
        });
    }

    // 获取列表长度
    @Override
    public int getItemCount() {
        return bookList.size();
    }

    // ViewHolder类（绑定列表项控件）
    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        TextView tvAuthor;
        Button btnAdd;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_book_cover);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvAuthor = itemView.findViewById(R.id.tv_book_author);
            btnAdd = itemView.findViewById(R.id.btn_add_to_bookshelf);
        }

    }
}
