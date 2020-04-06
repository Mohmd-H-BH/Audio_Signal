package com.testapp.audiosignal;

public interface Contract {

    interface View{

        void showProgress();

        void hideProgress();

        //void showMessage(String string);

        void showError(int resId);

    }

    interface UserActionsListener<T extends View>{

        void bindView(T view);

        void unbindView();

        void clear();

    }

}
