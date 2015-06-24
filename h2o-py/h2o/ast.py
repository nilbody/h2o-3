"""
This module contains code for the lazy expression DAG.
"""

import h2o


class ExprNode:
  """ Composable Expressions """

  def __init__(self,op,*args):
    self._op=op                # unary/binary/prefix op
    self._children=[ExprNode._arg_to_expr(a) for a in args]  # a list of ExprNode instances; the children of "this" node; (e.g. (+ left rite)  self._children = [left,rite] )

  def _eager(self,sb=None):
    """ This call is mutually recursive with ExprNode._do_it and H2OFrame._do_it """
    if sb is None: sb = []
    sb += ["("+self._op+" "]
    for child in self._children: ExprNode._do_it(child,sb)
    sb += [") "]
    return sb

  @staticmethod
  def _do_it(child,sb):
    if isinstance(child, h2o.H2OFrame): child._do_it(sb)
    elif isinstance(child, ExprNode):   child._eager(sb)
    else:                               sb+=[str(child)+" "]

  @staticmethod
  def _arg_to_expr(arg):
    if isinstance(arg, (int, float, ExprNode, h2o.H2OFrame)): return arg
    elif isinstance(arg, str):                                return '"'+arg+'"'
    elif isinstance(arg, slice):                              return "[{}:{}]".format(arg.start,arg.stop)
    raise ValueError("Unexpected arg type: " + str(type(arg)))

  @staticmethod
  def _collapse_sb(sb): return ' '.join("".join(sb).replace("\n", "").split()).replace(" )", ")")

  def _debug_print(self,pprint=True): return "".join(self._to_string(sb=[])) if pprint else ExprNode._collapse_sb(self._to_string(sb=[]))

  def _to_string(self,depth=0,sb=None):
    sb += ['\n', " "*depth, "("+self._op, " "]
    for child in self._children:
      if isinstance(child, h2o.H2OFrame) and not child._computed: child._ast._to_string(depth+2,sb)
      elif isinstance(child, ExprNode):                           child._to_string(depth+2,sb)
      else:                                                       sb+=['\n', ' '*(depth+2), str(child)]
    sb+=['\n',' '*depth+") "] + ['\n'] * (depth==0)  # add a \n if depth == 0
    return sb