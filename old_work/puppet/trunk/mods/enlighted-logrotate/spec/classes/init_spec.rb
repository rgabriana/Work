require 'spec_helper'
describe 'logrotate' do

  context 'with defaults for all parameters' do
    it { should contain_class('logrotate') }
  end
end
