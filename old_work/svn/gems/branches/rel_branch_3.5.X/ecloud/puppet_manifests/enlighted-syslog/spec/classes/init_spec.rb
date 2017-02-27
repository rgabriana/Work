require 'spec_helper'
describe 'syslog' do

  context 'with defaults for all parameters' do
    it { should contain_class('syslog') }
  end
end
