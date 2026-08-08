package com.amedick.hospitalapp.adapters;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u0015B'\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u0012\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\u0002\u0010\tJ\b\u0010\n\u001a\u00020\u000bH\u0016J\u001c\u0010\f\u001a\u00020\b2\n\u0010\r\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u000e\u001a\u00020\u000bH\u0016J\u001c\u0010\u000f\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u000bH\u0016J\u0014\u0010\u0013\u001a\u00020\b2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/amedick/hospitalapp/adapters/AppointmentAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/amedick/hospitalapp/adapters/AppointmentAdapter$AppointmentViewHolder;", "appointments", "", "Lcom/amedick/hospitalapp/models/Appointment;", "onCancelClick", "Lkotlin/Function1;", "", "(Ljava/util/List;Lkotlin/jvm/functions/Function1;)V", "getItemCount", "", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "updateData", "newAppointments", "AppointmentViewHolder", "app_release"})
public final class AppointmentAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.amedick.hospitalapp.adapters.AppointmentAdapter.AppointmentViewHolder> {
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.amedick.hospitalapp.models.Appointment> appointments;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.amedick.hospitalapp.models.Appointment, kotlin.Unit> onCancelClick = null;
    
    public AppointmentAdapter(@org.jetbrains.annotations.NotNull()
    java.util.List<com.amedick.hospitalapp.models.Appointment> appointments, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.amedick.hospitalapp.models.Appointment, kotlin.Unit> onCancelClick) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.amedick.hospitalapp.adapters.AppointmentAdapter.AppointmentViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.amedick.hospitalapp.adapters.AppointmentAdapter.AppointmentViewHolder holder, int position) {
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    public final void updateData(@org.jetbrains.annotations.NotNull()
    java.util.List<com.amedick.hospitalapp.models.Appointment> newAppointments) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/amedick/hospitalapp/adapters/AppointmentAdapter$AppointmentViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/amedick/hospitalapp/databinding/ItemAppointmentBinding;", "(Lcom/amedick/hospitalapp/adapters/AppointmentAdapter;Lcom/amedick/hospitalapp/databinding/ItemAppointmentBinding;)V", "bind", "", "appointment", "Lcom/amedick/hospitalapp/models/Appointment;", "app_release"})
    public final class AppointmentViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final com.amedick.hospitalapp.databinding.ItemAppointmentBinding binding = null;
        
        public AppointmentViewHolder(@org.jetbrains.annotations.NotNull()
        com.amedick.hospitalapp.databinding.ItemAppointmentBinding binding) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull()
        com.amedick.hospitalapp.models.Appointment appointment) {
        }
    }
}