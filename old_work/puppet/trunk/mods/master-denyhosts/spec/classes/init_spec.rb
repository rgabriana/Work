require 'spec_helper'
describe 'denyhosts' do

  context 'with defaults for all parameters' do
    it { should contain_class('denyhosts') }
  end
end
