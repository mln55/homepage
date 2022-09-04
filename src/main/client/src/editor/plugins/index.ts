import defaultPlugins from 'suneditor/src/plugins';
import pluginCode from './code';
import pluginHr from './hr';
import pluginSubmit from './submit';

export default {
  ...defaultPlugins,
  pluginCode,
  pluginHr,
  pluginSubmit
}
