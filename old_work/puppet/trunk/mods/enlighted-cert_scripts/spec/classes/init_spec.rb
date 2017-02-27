require 'spec_helper'
describe 'cert_scripts' do

  context 'with defaults for all parameters' do
    it { should contain_class('cert_scripts') }
  end
end
