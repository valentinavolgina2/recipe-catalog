
//CSD 230 Final Project - Valentina Volgina

package edu.lwtech.finalp;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class TypeDialogFragmant extends DialogFragment{

    public String catalogName = "";

    // Host activity must implement
    public interface OnTypeEnteredListener {
        void onTypeEntered(String subject);
        void onTypeEdited(String subject);
    }

    private TypeDialogFragmant.OnTypeEnteredListener mListener;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {

        final EditText subjectEditText = new EditText(getActivity());
        subjectEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        subjectEditText.setMaxLines(1);
        subjectEditText.setText(catalogName);

        int title;
        if(catalogName.equals(""))
            title = R.string.new_type;
        else
            title = R.string.edit_type;

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(subjectEditText)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String type = subjectEditText.getText().toString().trim();

                        if(catalogName.equals("")){
                            mListener.onTypeEntered(type);
                        }else{
                            mListener.onTypeEdited(type);
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (TypeDialogFragmant.OnTypeEnteredListener) context;
    }

}
