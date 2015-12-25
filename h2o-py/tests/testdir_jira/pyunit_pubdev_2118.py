from builtins import str
from builtins import zip
from builtins import range
import sys
sys.path.insert(1,"../../")
import h2o
from tests import pyunit_utils
from h2o.estimators.gbm import H2OGradientBoostingEstimator


def mycomp(l,r):
    assert len(l) == len(r)
    for i in range(len(l)):
        l_i = [num for num in l[i] if isinstance(num, (int,float))]
        r_i = [num for num in r[i] if isinstance(num, (int,float))]
        zz = list(zip(l_i,r_i))
        z = [abs(li-ri)<1e-8 for li,ri in zz]
        assert all(z), str(i) + ":" +  str(z)

def pubdev_2118():
    df = h2o.import_file(pyunit_utils.locate("smalldata/prostate/prostate.csv"))
    df["CAPSULE"]=df["CAPSULE"].asfactor()

    m = H2OGradientBoostingEstimator()
    m.train(x=df.names,y="CAPSULE", training_frame=df, validation_frame=df)

    t = m.gains_lift()
    #print t.cell_values
    exp = [(u'', 1, 0.010526315789473684, 0.9645152115701061, 2.4836601307189543, 2.4836601307189543, 1.0, 1.0, 0.026143790849673203, 0.026143790849673203, 148.36601307189542, 148.36601307189542), (u'', 2, 0.021052631578947368, 0.9594222354576036, 2.4836601307189543, 2.4836601307189543, 1.0, 1.0, 0.026143790849673203, 0.05228758169934641, 148.36601307189542, 148.36601307189542), (u'', 3, 0.031578947368421054, 0.9554319405480538, 2.4836601307189543, 2.4836601307189543, 1.0, 1.0, 0.026143790849673203, 0.0784313725490196, 148.36601307189542, 148.36601307189542), (u'', 4, 0.042105263157894736, 0.944884578586349, 2.4836601307189543, 2.4836601307189543, 1.0, 1.0, 0.026143790849673203, 0.10457516339869281, 148.36601307189542, 148.36601307189542), (u'', 5, 0.05, 0.9403689587093514, 2.4836601307189543, 2.4836601307189543, 1.0, 1.0, 0.0196078431372549, 0.12418300653594772, 148.36601307189542, 148.36601307189542), (u'', 6, 0.1, 0.9102746372545834, 2.4836601307189543, 2.4836601307189543, 1.0, 1.0, 0.12418300653594772, 0.24836601307189543, 148.36601307189542, 148.36601307189542), (u'', 7, 0.15, 0.8422452177150065, 2.4836601307189543, 2.4836601307189543, 1.0, 1.0, 0.12418300653594772, 0.37254901960784315, 148.36601307189542, 148.36601307189542), (u'', 8, 0.2, 0.7775490475924511, 2.4836601307189543, 2.4836601307189543, 1.0, 1.0, 0.12418300653594772, 0.49673202614379086, 148.36601307189542, 148.36601307189542), (u'', 9, 0.3, 0.6635338768660201, 2.4183006535947715, 2.4618736383442266, 0.9736842105263158, 0.9912280701754386, 0.24183006535947713, 0.738562091503268, 141.83006535947715, 146.18736383442265), (u'', 10, 0.4, 0.43483448452697165, 1.6993464052287583, 2.27124183006536, 0.6842105263157895, 0.9144736842105263, 0.16993464052287582, 0.9084967320261438, 69.93464052287584, 127.12418300653599), (u'', 11, 0.5, 0.28860754164280733, 0.8496732026143792, 1.9869281045751637, 0.34210526315789475, 0.8, 0.08496732026143791, 0.9934640522875817, -15.032679738562083, 98.69281045751637), (u'', 12, 0.6, 0.19162079485580272, 0.06535947712418301, 1.6666666666666667, 0.02631578947368421, 0.6710526315789473, 0.006535947712418301, 1.0, -93.4640522875817, 66.66666666666667), (u'', 13, 0.7, 0.12236094473302055, 0.0, 1.4285714285714286, 0.0, 0.575187969924812, 0.0, 1.0, -100.0, 42.85714285714286), (u'', 14, 0.8, 0.08707286495062161, 0.0, 1.25, 0.0, 0.5032894736842105, 0.0, 1.0, -100.0, 25.0), (u'', 15, 0.9, 0.05274960908489455, 0.0, 1.1111111111111112, 0.0, 0.4473684210526316, 0.0, 1.0, -100.0, 11.111111111111116), (u'', 16, 1.0, 0.010219010683141726, 0.0, 1.0, 0.0, 0.4026315789473684, 0.0, 1.0, -100.0, 0.0)]
    mycomp(exp, t.cell_values)

    t = m.gains_lift(valid=True)
    mycomp(exp, t.cell_values)

    p = m.model_performance(df)
    t = p.gains_lift()
    mycomp(exp, t.cell_values)


    m = H2OGradientBoostingEstimator(nfolds=3, seed=1234)
    m.train(x=df.names,y="CAPSULE", training_frame=df, validation_frame=df, seed = 1234)
    t = m.gains_lift(xval=True)

    #print t.cell_values
    exp2 = [(u'', 1, 0.010526315789473684, 0.9640496540302931, 1.8627450980392157, 1.8627450980392157, 0.75, 0.75, 0.0196078431372549, 0.0196078431372549, 86.27450980392157, 86.27450980392157), (u'', 2, 0.021052631578947368, 0.9606052933149544, 1.8627450980392157, 1.8627450980392157, 0.75, 0.75, 0.0196078431372549, 0.0392156862745098, 86.27450980392157, 86.27450980392157), (u'', 3, 0.031578947368421054, 0.9475623015771373, 1.8627450980392157, 1.8627450980392157, 0.75, 0.75, 0.0196078431372549, 0.058823529411764705, 86.27450980392157, 86.27450980392157), (u'', 4, 0.042105263157894736, 0.9397623471806399, 1.8627450980392157, 1.8627450980392157, 0.75, 0.75, 0.0196078431372549, 0.0784313725490196, 86.27450980392157, 86.27450980392157), (u'', 5, 0.05, 0.9293601016531292, 2.4836601307189543, 1.9607843137254903, 1.0, 0.7894736842105263, 0.0196078431372549, 0.09803921568627451, 148.36601307189542, 96.07843137254903), (u'', 6, 0.1, 0.8518349676681573, 2.0915032679738563, 2.026143790849673, 0.8421052631578947, 0.8157894736842105, 0.10457516339869281, 0.20261437908496732, 109.15032679738563, 102.61437908496731), (u'', 7, 0.15, 0.7889653619147378, 1.6993464052287583, 1.9172113289760349, 0.6842105263157895, 0.7719298245614035, 0.08496732026143791, 0.2875816993464052, 69.93464052287584, 91.72113289760348), (u'', 8, 0.2, 0.7115289412185587, 1.8300653594771241, 1.8954248366013073, 0.7368421052631579, 0.7631578947368421, 0.0915032679738562, 0.3790849673202614, 83.00653594771241, 89.54248366013073), (u'', 9, 0.3, 0.5512095070046575, 1.5686274509803921, 1.786492374727669, 0.631578947368421, 0.7192982456140351, 0.1568627450980392, 0.5359477124183006, 56.86274509803921, 78.64923747276691), (u'', 10, 0.4, 0.4393311795322961, 1.3071895424836601, 1.6666666666666667, 0.5263157894736842, 0.6710526315789473, 0.13071895424836602, 0.6666666666666666, 30.718954248366014, 66.66666666666667), (u'', 11, 0.5, 0.31901370172923155, 0.9150326797385621, 1.516339869281046, 0.3684210526315789, 0.6105263157894737, 0.0915032679738562, 0.7581699346405228, -8.496732026143794, 51.63398692810459), (u'', 12, 0.6, 0.21433587868992393, 0.9150326797385621, 1.4161220043572986, 0.3684210526315789, 0.5701754385964912, 0.0915032679738562, 0.8496732026143791, -8.496732026143794, 41.61220043572986), (u'', 13, 0.7, 0.12787120888155754, 0.5228758169934641, 1.288515406162465, 0.21052631578947367, 0.518796992481203, 0.05228758169934641, 0.9019607843137255, -47.71241830065359, 28.851540616246506), (u'', 14, 0.8, 0.0890968313518409, 0.5882352941176471, 1.2009803921568627, 0.23684210526315788, 0.48355263157894735, 0.058823529411764705, 0.9607843137254902, -41.17647058823529, 20.09803921568627), (u'', 15, 0.9, 0.04544565902678719, 0.06535947712418301, 1.074800290486565, 0.02631578947368421, 0.4327485380116959, 0.006535947712418301, 0.9673202614379085, -93.4640522875817, 7.4800290486565), (u'', 16, 1.0, 0.015228167105626272, 0.32679738562091504, 1.0, 0.13157894736842105, 0.4026315789473684, 0.032679738562091505, 1.0, -67.3202614379085, 0.0)]
    mycomp(exp2, t.cell_values)


    p = m.model_performance(df)
    t = p.gains_lift()
    mycomp(exp, t.cell_values)


if __name__ == "__main__":
    pyunit_utils.standalone_test(pubdev_2118)
else:
    pubdev_2118()
