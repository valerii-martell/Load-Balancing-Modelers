using Android.App;
using Android.Widget;
using Android.OS;
using Android.Content.PM;
using System;
using System.Reflection;

namespace CommonBus
{
    [Activity(Label = "Common Bus",
     MainLauncher = true,
     Icon = "@drawable/icon",
     ConfigurationChanges = ConfigChanges.ScreenSize | ConfigChanges.Orientation,
     ScreenOrientation = ScreenOrientation.Landscape)]
    public class MainActivity : Activity
    {
        private int procCount = 0;
        protected override void OnCreate(Bundle bundle)
        {
            base.OnCreate(bundle);

            SetContentView(Resource.Layout.Main);

            var textProcessorsCount = FindViewById<EditText>(Resource.Id.editTextProcessorsCount);
            TableLayout tableInput = (TableLayout)FindViewById<TableLayout>(Resource.Id.systemParametersTable);

            textProcessorsCount.TextChanged += (e, o) =>
            {
                try
                {
                    procCount = Int32.Parse(textProcessorsCount.Text);
                    TableRow tableRow = new TableRow(this);
                    tableRow.LayoutParameters = new TableRow.LayoutParams(TableRow.LayoutParams.FillParent, TableRow.LayoutParams.WrapContent);

                    Button b = new Button(this);
                    //b.setText("Dynamic Button");
                    //b.tLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FillParent, TableRow.LayoutParams.WrapContent));
                    /* Add Button to row. 
                    tr.addView(b);
                    /* Add row to TableLayout. 
                    //tr.setBackgroundResource(R.drawable.sf_gradient_03);
                    tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

                    }
                    catch { }
                }
                    /*
                    tableInput.LayoutParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FillParent,
            LinearLayout.LayoutParams.FillParent);


                    /* Create a new row to be added. 
                    TableRow tr = new TableRow(this);
                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    /* Create a Button to be the row-content. 
                    Button b = new Button(this);
                    b.setText("Dynamic Button");
                    b.tLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FillParent, TableRow.LayoutParams.WrapContent));
                    /* Add Button to row. 
                    tr.addView(b);
                    /* Add row to TableLayout. 
                    //tr.setBackgroundResource(R.drawable.sf_gradient_03);
                    tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                    */
                }
                catch
                {
                }
            };



                // Set our view from the "main" layout resource
                // SetContentView (Resource.Layout.Main);
            }
    }
    
}

