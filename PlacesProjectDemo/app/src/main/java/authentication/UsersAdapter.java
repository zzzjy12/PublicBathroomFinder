package authentication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.placesprojectdemo.R;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{

    private Context context;
    private List<UserAccount> users;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(UserAccount user);
    }

    public UsersAdapter(Context context, List<UserAccount> users, OnItemClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserAccount user = users.get(position);
        holder.emailTextView.setText(user.getEmail());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;

        public UserViewHolder(View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.text_view_email);
        }
    }
}
