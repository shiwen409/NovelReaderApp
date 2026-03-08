package com.example.novelreaderapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.novelreaderapp.R;
import com.example.novelreaderapp.entity.User;

import java.util.List;

public class UserAdapter extends BaseAdapter {
    private Context context;
    private List<User> userList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onDelete(String username);
        void onEdit(User user);
    }

    public void setOnUserActionListener(OnUserActionListener listener) {
        this.listener = listener;
    }

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
            holder = new ViewHolder();
            holder.tvUsername = convertView.findViewById(R.id.tv_username);
            holder.tvPassword = convertView.findViewById(R.id.tv_password);
            holder.btnEdit = convertView.findViewById(R.id.btn_edit);
            holder.btnDelete = convertView.findViewById(R.id.btn_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User user = userList.get(position);
        holder.tvUsername.setText("用户名: " + user.getUsername());
        holder.tvPassword.setText("密码: " + user.getPassword());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(user);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(user.getUsername());
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView tvUsername;
        TextView tvPassword;
        Button btnEdit;
        Button btnDelete;
    }
}